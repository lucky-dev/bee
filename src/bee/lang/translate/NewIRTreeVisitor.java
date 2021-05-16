package bee.lang.translate;

import bee.lang.Constants;
import bee.lang.ast.*;
import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.*;
import bee.lang.symtable.*;
import bee.lang.translate.frame.Access;
import bee.lang.translate.frame.Frame;
import bee.lang.translate.frame.InFrame;
import bee.lang.translate.ir.Ex;
import bee.lang.translate.ir.Nx;
import bee.lang.translate.ir.RelCx;
import bee.lang.translate.ir.WrapperIRExpression;
import bee.lang.visitors.IRTreeVisitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

// This visitor converts AST nodes to IR nodes. This visitor uses the class 'WrapperIRExpression'. This class is a base class for other wrapper-classes 'Cx', 'Ex', 'Nx', 'RelCx'.
// Wrapper-classes are used to convert IR nodes depending on context. E.g. in the code like this:
//      var x : bool = true;
//      ...
//      if (x = someMethod()) { ... }
// The expression 'x = someMethod()' must return a value (true or false) for the statement 'if'.
// But in the next code:
//      var y : bool;
//      ...
//      y = someMethod();
// The statement 'y = someMethod();' must not return any value.
// So for the first case 'NewIRTreeVisitor' uses the class 'Ex'. For the second case 'NewIRTreeVisitor' uses the class 'Nx'.
// The classes 'Cx' and 'RelCx' are wrappers for logical expressions like this: x < 5, x != y and etc. These classes use labels (true or false) during converting logical expressions.

// Notes: arrays are represented in heap: | size of array | element 0 | element 1 | element 2 | element N |. So a size of array is N + 1.
// Objects are represented in heap: | size of object | pointer to class description | field 0 | field 1 | field 2 | field N |.
// Class description in static memory: | pointer to vtable | static field 0 | static field 1 | static field 2 | static field N |.
// VTable in static memory: | address of virtual method 0 | address of virtual method 1 | address of virtual method 2 | address of virtual method N |.

public class NewIRTreeVisitor implements IRTreeVisitor {

    private Label mCurrentLblEnd;
    private Label mCurrentLblBeginLoop;
    private Label mClassDescriptionLbl;
    private HashMap<String, EntityLayout> mObjectLayouts;
    private HashMap<String, EntityLayout> mClassLayouts;
    private HashMap<String, EntityLayout> mMethodLayouts;
    private ClassSymbol mCurrentClassSymbol;
    private LinkedList<Fragment> mListFragments;
    private EntityLayout mClassLayout;
    private EntityLayout mObjectLayout;
    private boolean isFieldDefinition;
    private boolean isStaticFieldDefinition;
    private String mCurrentFieldId;
    private Frame mCurrentFrame;
    private Frame mInitFieldsFrame;
    private Frame mInitStaticFieldsFrame;
    private IRStatement mBodyMethodInitFields;
    private SEQ mLastStatementBodyMethodInitFields;
    private IRStatement mBodyMethodInitStaticFields;
    private SEQ mLastStatementBodyMethodInitStaticFields;
    private HashMap<String, Access> mListLocalVars;
    private Label mMethodReturnLbl;
    private boolean isReturnStatement;
    private Access mFirstArgInFunction;
    private Frame mFrame;
    private LinkedList<String> mListOfMethodsInitStaticFields;

    public NewIRTreeVisitor(Frame frame, HashMap<String, EntityLayout> objectLayouts, HashMap<String, EntityLayout> classLayouts, HashMap<String, EntityLayout> methodLayouts) {
        mFrame = frame;
        mObjectLayouts = objectLayouts;
        mClassLayouts = classLayouts;
        mMethodLayouts = methodLayouts;
        // The list of fragments keeps all procedures and data.
        mListFragments = new LinkedList<>();
        // These maps connect all local variables of a method with objects of the class Access.
        mListLocalVars = new HashMap<>();
        mFirstArgInFunction = new InFrame(0);
        mListOfMethodsInitStaticFields = new LinkedList<>();
    }

    public LinkedList<Fragment> getFragments() {
        return mListFragments;
    }

    public LinkedList<String> getListOfMethodsInitStaticFields() {
        return mListOfMethodsInitStaticFields;
    }

    @Override
    public WrapperIRExpression visit(Add expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.PLUS, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(And expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        Temp result = new Temp();
        Label lblTrue = Label.newLabel();
        Label lblFalse = Label.newLabel();
        Label lblEnd = Label.newLabel();

        return new Ex(
                new ESEQ(
                        new SEQ(
                                new MOVE(new TEMP(result), new CONST(1)),
                                new SEQ(
                                        new CJUMP(TypeRelOp.EQ, leftExpression.unEx(), new CONST(1), lblTrue, lblFalse),
                                        new SEQ(
                                                new LABEL(lblTrue),
                                                new SEQ(
                                                        new CJUMP(TypeRelOp.EQ, rightExpression.unEx(), new CONST(1), lblEnd, lblFalse),
                                                        new SEQ(
                                                                new LABEL(lblFalse),
                                                                new SEQ(
                                                                        new MOVE(new TEMP(result), new CONST(0)),
                                                                        new LABEL(lblEnd)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new TEMP(result)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(ArrayAccess expression) {
        WrapperIRExpression arrayExpression = expression.getExpression().visit(this);
        WrapperIRExpression arrayIndex = expression.getIndex().visit(this);

        Temp index = new Temp();
        Temp size = new Temp();
        Label lblTrue1 = Label.newLabel();
        Label lblFalse = Label.newLabel();
        Label lblTrue2 = Label.newLabel();

        RelCx lowBound = new RelCx(TypeRelOp.GE, new TEMP(index), new CONST(0));
        RelCx hiBound = new RelCx(TypeRelOp.LT, new TEMP(index), new TEMP(size));

        return new Ex(
                new ESEQ(
                        new MOVE(new TEMP(index), arrayIndex.unEx()),
                        new ESEQ(
                                new MOVE(new TEMP(size), new MEM(arrayExpression.unEx())),
                                new ESEQ(
                                        new CJUMP(TypeRelOp.EQ, hiBound.unEx(), new CONST(1), lblTrue1, lblFalse),
                                        new ESEQ(
                                                new LABEL(lblTrue1),
                                                new ESEQ(
                                                        new CJUMP(TypeRelOp.EQ, lowBound.unEx(), new CONST(1), lblTrue2, lblFalse),
                                                        new ESEQ(
                                                                new SEQ(
                                                                        new LABEL(lblFalse),
                                                                        new EXP(mCurrentFrame.externalCall(Constants.FUNCTION_PRINT_ERROR, args(new CONST(0))))
                                                                ),
                                                                new ESEQ(
                                                                        new LABEL(lblTrue2),
                                                                        new MEM(new BINOP(TypeBinOp.PLUS, arrayExpression.unEx(), new BINOP(TypeBinOp.MUL, new BINOP(TypeBinOp.PLUS, new TEMP(index), new CONST(1)), new CONST(mCurrentFrame.getWordSize()))))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    @Override
    public WrapperIRExpression visit(Assignment expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        Temp result = new Temp();

        return new Ex(
                new ESEQ(
                    new SEQ(new MOVE(new TEMP(result), rightExpression.unEx()),
                            new MOVE(leftExpression.unEx(), new TEMP(result))),
                    new TEMP(result)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(AssignmentStatement statement) {
        return statement.getExpression().visit(this);
    }

    @Override
    public WrapperIRExpression visit(Block statement) {
        return statement.getStatements().visit(this);
    }

    @Override
    public WrapperIRExpression visit(BoolLiteral expression) {
        return new Ex(new CONST(expression.getValue() ? 1 : 0));
    }

    @Override
    public WrapperIRExpression visit(Break statement) {
        return new Nx(new JUMP(mCurrentLblEnd));
    }

    @Override
    public WrapperIRExpression visit(Continue statement) {
        return new Nx(new JUMP(mCurrentLblBeginLoop));
    }

    @Override
    public WrapperIRExpression visit(Call expression) {
        MethodSymbol methodSymbol = (MethodSymbol) expression.getSymbol();

        if (methodSymbol.isStatic()) {
            return new Ex(new CALL(new NAME(Label.newLabel(methodSymbol.getMethodId())), args(expression.getArgumentsList().getExpressionList())));
        } else {
            Temp currentObject = new Temp();
            MOVE initCurrentObject = new MOVE(new TEMP(currentObject), expression.getExpression().visit(this).unEx());
            // The first argument is the current object aka 'this'.
            LinkedList<IRExpression> args = args(new TEMP(currentObject), expression.getArgumentsList().getExpressionList());

            if ((methodSymbol.isPrivate()) || (expression.getExpression() instanceof Super)) {
                return new Ex(
                        new ESEQ(
                                initCurrentObject,
                                new CALL(new NAME(Label.newLabel(methodSymbol.getMethodId())), args)
                        )
                );
            } else {
                int virtualMethodId = mMethodLayouts.get(((ClassSymbol) methodSymbol.getEnclosingScope()).getIdentifier().getName()).get(methodSymbol.getMethodId());
                return new Ex(
                        new ESEQ(
                                initCurrentObject,
                                new CALL(
                                        new MEM(new BINOP(TypeBinOp.PLUS, new MEM(new MEM(new BINOP(TypeBinOp.PLUS, new TEMP(currentObject), new CONST(mCurrentFrame.getWordSize())))), new CONST(virtualMethodId * mCurrentFrame.getWordSize()))),
                                        args
                                )
                        )
                );
            }
        }
    }

    @Override
    public WrapperIRExpression visit(CharLiteral expression) {
        return new Ex(new CONST(expression.getValue()));
    }

    @Override
    public WrapperIRExpression visit(ClassDefinition statement) {
        mCurrentClassSymbol = (ClassSymbol) statement.getSymbol();

        String className = mCurrentClassSymbol.getIdentifier().getName();
        mClassLayout = mClassLayouts.get(className);
        mObjectLayout = mObjectLayouts.get(className);

        mClassDescriptionLbl = Label.newLabel(String.format(Constants.CLASS_DESCRIPTION, className));

        // Create a new method like this '_<class name>_init_static_fields'. This method is used to initialize all static fields.
        // This method will be called immediately for loaded class.
        String nameOfMethod = String.format(Constants.FUNCTION_INIT_STATIC_FIELDS, className);
        mListOfMethodsInitStaticFields.add(nameOfMethod);
        mInitStaticFieldsFrame = mFrame.newFrame(Label.newLabel(nameOfMethod), new LinkedList<>());

        // Create a new method like this '_<class name>_init_fields'. This method is used to initialize all non-static fields.
        // This method will be called by a constructor after calling a super constructor.
        mInitFieldsFrame = mFrame.newFrame(Label.newLabel(String.format(Constants.FUNCTION_INIT_FIELDS, className)), args(false));

        statement.getFieldDefinitions().visit(this);

        if (mBodyMethodInitStaticFields == null) {
            mBodyMethodInitStaticFields = new EXP(new CONST(0));
        } else {
            mLastStatementBodyMethodInitStaticFields.setRightStatement(new EXP(new CONST(0)));
        }

        if (mBodyMethodInitFields == null) {
            mBodyMethodInitFields = new EXP(new CONST(0));
        } else {
            mLastStatementBodyMethodInitFields.setRightStatement(new EXP(new CONST(0)));
        }

        // Creating bodies for '_<class name>_init_static_fields' and '_<class name>_init_fields' is performed during processing of fields definitions.
        mListFragments.add(new ProcedureFragment(mInitStaticFieldsFrame.procEntryExit1(mBodyMethodInitStaticFields), mInitStaticFieldsFrame));
        mListFragments.add(new ProcedureFragment(mInitFieldsFrame.procEntryExit1(mBodyMethodInitFields), mInitFieldsFrame));

        statement.getConstructorDefinitions().visit(this);
        statement.getMethodDefinitions().visit(this);

        mBodyMethodInitStaticFields = null;
        mLastStatementBodyMethodInitStaticFields = null;
        mBodyMethodInitFields = null;
        mLastStatementBodyMethodInitFields = null;

        mClassLayout = null;
        mObjectLayout = null;

        mCurrentClassSymbol = null;

        return null;
    }

    @Override
    public WrapperIRExpression visit(ConstructorDefinition statement) {
        String methodName = ((MethodSymbol) statement.getSymbol()).getMethodId();

        Iterator<Statement> statementsIterator = statement.getFormalArgumentsList().getStatementsList().iterator();
        LinkedList<Boolean> procedureArgs = new LinkedList<>();
        procedureArgs.add(false);
        while (statementsIterator.hasNext()) {
            procedureArgs.add(false);
            statementsIterator.next();
        }

        mCurrentFrame = mFrame.newFrame(Label.newLabel(methodName), procedureArgs);

        statementsIterator = statement.getFormalArgumentsList().getStatementsList().iterator();
        Iterator<Access> formalArgsIterator = mCurrentFrame.getFormalArgumentsInFunction().iterator();

        // Need to skip first argument because it is the current object.
        if (formalArgsIterator.hasNext()) {
            formalArgsIterator.next();
        }

        while ((statementsIterator.hasNext()) && (formalArgsIterator.hasNext())) {
            mListLocalVars.put(((LocalVariableSymbol) ((VariableDefinition) statementsIterator.next()).getSymbol()).getVarId(), formalArgsIterator.next());
        }

        statement.getFormalArgumentsList().visit(this);

        WrapperIRExpression tree = statement.getBody().visit(this);

        IRStatement callsOtherConstructors;

        if (statement.getSuperConstructorArgumentsList() != null) {
            IRStatement callInitFields = new EXP(new CALL(new NAME(Label.newLabel(String.format(Constants.FUNCTION_INIT_FIELDS, mCurrentClassSymbol.getIdentifier().getName()))), args(mFirstArgInFunction.exp(new TEMP(mCurrentFrame.getFP())))));

            if (mCurrentClassSymbol.getBaseClassIdentifier() != null) {
                String superConstructorId = ((MethodSymbol) statement.getOtherConstructorSymbol()).getMethodId();

                callsOtherConstructors = new SEQ(
                        new EXP(new CALL(new NAME(Label.newLabel(superConstructorId)), args(mFirstArgInFunction.exp(new TEMP(mCurrentFrame.getFP())), statement.getSuperConstructorArgumentsList().getExpressionList()))),
                        callInitFields
                );
            } else {
                callsOtherConstructors = callInitFields;
            }
        } else {
            String otherConstructorId = ((MethodSymbol) statement.getOtherConstructorSymbol()).getMethodId();

            callsOtherConstructors = new EXP(new CALL(new NAME(Label.newLabel(otherConstructorId)), args(mFirstArgInFunction.exp(new TEMP(mCurrentFrame.getFP())), statement.getOtherConstructorArgumentsList().getExpressionList())));
        }

        tree = new Nx(
                new SEQ(
                        callsOtherConstructors,
                        tree != null ? tree.unNx() : new EXP(new CONST(0))
                )
        );

        mListFragments.add(new ProcedureFragment(mCurrentFrame.procEntryExit1(tree.unNx()), mCurrentFrame));

        return null;
    }

    @Override
    public WrapperIRExpression visit(Div expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.DIV, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(DoWhile statement) {
        Label lblEnd = Label.newLabel();

        Label lblBeginLoop = Label.newLabel();

        Label savedLblEnd = mCurrentLblEnd;
        mCurrentLblEnd = lblEnd;

        Label saveLblBeginLoop = mCurrentLblBeginLoop;
        mCurrentLblBeginLoop = lblBeginLoop;

        WrapperIRExpression expression = statement.getExpression().visit(this);
        WrapperIRExpression body = statement.getStatement().visit(this);

        mCurrentLblEnd = savedLblEnd;

        mCurrentLblBeginLoop = saveLblBeginLoop;

        return new Nx(
                new SEQ(
                        new SEQ(
                                new SEQ(
                                        new LABEL(lblBeginLoop),
                                        body != null ? body.unNx() : new EXP(new CONST(0))
                                ),
                                new CJUMP(TypeRelOp.EQ, expression.unEx(), new CONST(1), lblBeginLoop, lblEnd)
                        ),
                        new LABEL(lblEnd)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(Equal expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.EQ, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(FieldAccess expression) {
        IRExpression irExpression = expression.getExpression().visit(this).unEx();

        FieldSymbol fieldSymbol = (FieldSymbol) expression.getSymbol();

        if (fieldSymbol.isStatic()) {
            return new Ex(new MEM(new BINOP(TypeBinOp.PLUS, irExpression, new CONST((mClassLayouts.get(fieldSymbol.getClassName()).get(fieldSymbol.getFieldId()) + 1) * mCurrentFrame.getWordSize()))));
        } else {
            return new Ex(new MEM(new BINOP(TypeBinOp.PLUS, irExpression, new CONST((mObjectLayouts.get(fieldSymbol.getClassName()).get(fieldSymbol.getFieldId()) + 2) * mCurrentFrame.getWordSize()))));
        }
    }

    @Override
    public WrapperIRExpression visit(FieldDefinition statement) {
        isFieldDefinition = true;

        mCurrentFieldId = ((FieldSymbol) statement.getSymbol()).getFieldId();

        isStaticFieldDefinition = statement.isStatic();

        if (isStaticFieldDefinition) {
            mCurrentFrame = mInitStaticFieldsFrame;
        } else {
            mCurrentFrame = mInitFieldsFrame;
        }

        statement.getVariableDefinition().visit(this);

        isStaticFieldDefinition = false;

        mCurrentFieldId = null;

        isFieldDefinition = false;

        return null;
    }

    @Override
    public WrapperIRExpression visit(GreaterEqualThan expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.GE, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(GreaterThan expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.GT, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(Identifier expression) {
        Symbol symbol = expression.getSymbol();

        if (symbol instanceof FieldSymbol) {
            FieldSymbol fieldSymbol = (FieldSymbol) symbol;
            if (!fieldSymbol.isStatic()) {
                return new Ex(new MEM(new BINOP(TypeBinOp.PLUS, mFirstArgInFunction.exp(new TEMP(mCurrentFrame.getFP())), new CONST((mObjectLayouts.get(fieldSymbol.getClassName()).get(fieldSymbol.getFieldId()) + 2) * mCurrentFrame.getWordSize()))));
            }
        }

        if (symbol instanceof LocalVariableSymbol) {
            return new Ex(mListLocalVars.get(((LocalVariableSymbol) symbol).getVarId()).exp(new TEMP(mCurrentFrame.getFP())));
        }

        if (symbol instanceof ClassSymbol) {
            return new Ex(new NAME(Label.newLabel(String.format(Constants.CLASS_DESCRIPTION, symbol.getIdentifier().getName()))));
        }

        return null;
    }

    @Override
    public WrapperIRExpression visit(If statement) {
        WrapperIRExpression expression = statement.getExpression().visit(this);
        WrapperIRExpression thenStatement = statement.getThenStatement().visit(this);

        Label lblTrue = Label.newLabel();
        Label lblFalse = Label.newLabel();

        if (statement.getElseStatement() == null) {
            return new Nx(
                    new SEQ(
                            new CJUMP(TypeRelOp.EQ, expression.unEx(), new CONST(1), lblTrue, lblFalse),
                            new SEQ(
                                    new LABEL(lblTrue),
                                    new SEQ(
                                            thenStatement != null ? thenStatement.unNx() : new EXP(new CONST(0)),
                                            new LABEL(lblFalse)
                                    )
                            )
                    )
            );
        } else {
            WrapperIRExpression elseStatement = statement.getElseStatement().visit(this);

            Label lblEnd = Label.newLabel();

            return new Nx(
                    new SEQ(
                            new CJUMP(TypeRelOp.EQ, expression.unEx(), new CONST(1), lblTrue, lblFalse),
                            new SEQ(
                                    new LABEL(lblTrue),
                                    new SEQ(
                                            thenStatement != null ? thenStatement.unNx() : new EXP(new CONST(0)),
                                            new SEQ(
                                                    new JUMP(lblEnd),
                                                    new SEQ(
                                                            new LABEL(lblFalse),
                                                            new SEQ(
                                                                    elseStatement != null ? elseStatement.unNx() : new EXP(new CONST(0)),
                                                                    new LABEL(lblEnd)
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
            );
        }
    }

    @Override
    public WrapperIRExpression visit(IntLiteral expression) {
        return new Ex(new CONST(expression.getValue()));
    }

    @Override
    public WrapperIRExpression visit(LessEqualThan expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.LE, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(LessThan expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.LT, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(MethodDefinition statement) {
        String methodName = ((MethodSymbol) statement.getSymbol()).getMethodId();

        Iterator<Statement> statementsIterator = statement.getFormalArgumentsList().getStatementsList().iterator();
        LinkedList<Boolean> args = new LinkedList<>();

        if (!statement.isStatic()) {
            args.add(false);
        }

        while (statementsIterator.hasNext()) {
            args.add(false);
            statementsIterator.next();
        }

        mCurrentFrame = mFrame.newFrame(Label.newLabel(methodName), args);

        statementsIterator = statement.getFormalArgumentsList().getStatementsList().iterator();
        Iterator<Access> formalArgsIterator = mCurrentFrame.getFormalArgumentsInFunction().iterator();

        // If method is not static then need to skip first argument because it is the current object.
        if ((!statement.isStatic()) && (formalArgsIterator.hasNext())) {
            formalArgsIterator.next();
        }

        while ((statementsIterator.hasNext()) && (formalArgsIterator.hasNext())) {
            mListLocalVars.put(((LocalVariableSymbol) ((VariableDefinition) statementsIterator.next()).getSymbol()).getVarId(), formalArgsIterator.next());
        }

        statement.getFormalArgumentsList().visit(this);

        mMethodReturnLbl = Label.newLabel("_" + methodName + "_return_");

        isReturnStatement = false;

        IRStatement irExpression = statement.getBody().visit(this).unNx();

        mListFragments.add(new ProcedureFragment(mCurrentFrame.procEntryExit1(isReturnStatement ? new SEQ(irExpression, new LABEL(mMethodReturnLbl)) : irExpression), mCurrentFrame));

        return null;
    }

    @Override
    public WrapperIRExpression visit(Mod expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.MOD, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(NewArray expression) {
        Temp originalSize = new Temp();
        Temp newSize = new Temp();
        Temp newArray = new Temp();

        return new Ex(
                new ESEQ(
                        new SEQ(
                                new MOVE(new TEMP(originalSize), expression.getSize().visit(this).unEx()),
                                new SEQ(
                                        new MOVE(new TEMP(newSize), new BINOP(TypeBinOp.MUL, new BINOP(TypeBinOp.PLUS, new TEMP(originalSize), new CONST(1)), new CONST(mCurrentFrame.getWordSize()))),
                                        new SEQ(
                                                new MOVE(new TEMP(newArray), mCurrentFrame.externalCall(Constants.FUNCTION_ALLOC_INIT_RAW_MEMORY, args(new TEMP(newSize)))),
                                                new MOVE(new MEM(new TEMP(newArray)), new TEMP(originalSize))
                                        )
                                )
                        ),
                        new TEMP(newArray)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(NewObject expression) {
        MethodSymbol methodSymbol = (MethodSymbol) expression.getSymbol();

        Temp newObject = new Temp();

        LinkedList<IRExpression> args = args(new TEMP(newObject), expression.getArgumentsList().getExpressionList());

        // Count of all non-static fields + pointer to a class descriptor.
        int sizeOfObject = mObjectLayouts.get(((ClassSymbol) methodSymbol.getEnclosingScope()).getIdentifier().getName()).getCountItems() + 1;

        return new Ex(
                new ESEQ(
                        new SEQ(new MOVE(new TEMP(newObject), mCurrentFrame.externalCall(Constants.FUNCTION_ALLOC_INIT_RAW_MEMORY, args(new CONST((sizeOfObject + 1) * mCurrentFrame.getWordSize())))),
                                new SEQ(
                                        new MOVE(new MEM(new TEMP(newObject)), new CONST(sizeOfObject)),
                                        new SEQ(
                                                new MOVE(new MEM(new BINOP(TypeBinOp.PLUS, new TEMP(newObject), new CONST(mCurrentFrame.getWordSize()))), new NAME(Label.newLabel(String.format(Constants.CLASS_DESCRIPTION, expression.getType())))),
                                                new EXP(new CALL(new NAME(Label.newLabel(methodSymbol.getMethodId())), args))
                                        )
                                )
                        ),
                        new TEMP(newObject)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(Nil expression) {
        return new Ex(new CONST(0));
    }

    @Override
    public WrapperIRExpression visit(Not expression) {
        WrapperIRExpression expr = expression.getExpression().visit(this);

        Temp result = new Temp();
        Label lblTrue = Label.newLabel();
        Label lblFalse = Label.newLabel();

        return new Ex(
                new ESEQ(
                        new SEQ(
                                new MOVE(new TEMP(result), new CONST(1)),
                                new SEQ(
                                        new SEQ(new CJUMP(TypeRelOp.EQ, expr.unEx(), new CONST(1), lblTrue, lblFalse),
                                                new SEQ(
                                                        new LABEL(lblTrue),
                                                        new MOVE(new TEMP(result), new CONST(0)))
                                        ),
                                        new LABEL(lblFalse)
                                )
                        ),
                new TEMP(result))
        );
    }

    @Override
    public WrapperIRExpression visit(NotEqual expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.NE, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(Or expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        Temp result = new Temp();
        Label lblFalse = Label.newLabel();
        Label lblEnd = Label.newLabel();

        return new Ex(
                new ESEQ(
                        new SEQ(
                                new MOVE(new TEMP(result), new CONST(1)),
                                new SEQ(
                                        new CJUMP(TypeRelOp.EQ, leftExpression.unEx(), new CONST(1), lblEnd, lblFalse),
                                        new SEQ(
                                                new LABEL(lblFalse),
                                                new SEQ(
                                                        new CJUMP(TypeRelOp.EQ, rightExpression.unEx(), new CONST(1), lblEnd, lblFalse),
                                                        new SEQ(
                                                                new LABEL(lblFalse),
                                                                new SEQ(
                                                                        new MOVE(new TEMP(result), new CONST(0)),
                                                                        new LABEL(lblEnd)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new TEMP(result)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(Program statement) {
        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }

        return null;
    }

    @Override
    public WrapperIRExpression visit(Return statement) {
        isReturnStatement = true;

        if (statement.getExpression() == null) {
            return new Nx(new JUMP(mMethodReturnLbl));
        } else {
            return new Nx(new SEQ(
                    new MOVE(new TEMP(mCurrentFrame.getRV()), statement.getExpression().visit(this).unEx()),
                    new JUMP(mMethodReturnLbl)));
        }
    }

    @Override
    public WrapperIRExpression visit(Statements statement) {
        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        SEQ listSeqs = null;
        SEQ currentSeq = null;
        while (iterator.hasNext()) {
            WrapperIRExpression irExpression = iterator.next().visit(this);
            if (irExpression != null) {
                if (listSeqs == null) {
                    listSeqs = new SEQ();
                    currentSeq = listSeqs;
                } else {
                    SEQ newSeq = new SEQ();
                    currentSeq.setRightStatement(newSeq);
                    currentSeq = newSeq;
                }

                currentSeq.setLeftStatement(irExpression.unNx());
            }
        }

        if (currentSeq != null) {
            currentSeq.setRightStatement(new EXP(new CONST(0)));
        }

        return listSeqs != null ? new Nx(listSeqs) : new Nx(new EXP(new CONST(0)));
    }

    @Override
    public WrapperIRExpression visit(StringLiteral expression) {
        String str = expression.getValue();

        Label lblNewStr;
        if (Label.isLabelForString(str)) {
            lblNewStr = Label.newLabelForString(str);
        } else {
            lblNewStr = Label.newLabelForString(str);
            mListFragments.add(new StringFragment(lblNewStr.getName(), str));
        }

        return new Ex(mCurrentFrame.externalCall(Constants.FUNCTION_CONVERT_STRING_TO_ARRAY, args(new CONST(str.length() * mFrame.getWordSize()), new NAME(lblNewStr))));
    }

    @Override
    public WrapperIRExpression visit(Subtract expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.MINUS, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(Super expression) {
        return new Ex(mFirstArgInFunction.exp(new TEMP(mCurrentFrame.getFP())));
    }

    @Override
    public WrapperIRExpression visit(TernaryOperator expression) {
        WrapperIRExpression conditionalExpression = expression.getConditionalExpression().visit(this);
        WrapperIRExpression thenExpression = expression.getThenExpression().visit(this);
        WrapperIRExpression elseExpression = expression.getElseExpression().visit(this);

        Temp result = new Temp();
        Label lblTrue = Label.newLabel();
        Label lblFalse = Label.newLabel();
        Label lblEnd = Label.newLabel();

        return new Ex(
                new ESEQ(
                        new SEQ(
                                new CJUMP(TypeRelOp.EQ, conditionalExpression.unEx(), new CONST(1), lblTrue, lblFalse),
                                new SEQ(
                                        new LABEL(lblTrue),
                                        new SEQ(
                                                new MOVE(new TEMP(result), thenExpression.unEx()),
                                                new SEQ(
                                                        new JUMP(lblEnd),
                                                        new SEQ(
                                                                new LABEL(lblFalse),
                                                                new SEQ(
                                                                        new MOVE(new TEMP(result), elseExpression.unEx()),
                                                                        new LABEL(lblEnd)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new TEMP(result)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(This expression) {
        return new Ex(mFirstArgInFunction.exp(new TEMP(mCurrentFrame.getFP())));
    }

    @Override
    public WrapperIRExpression visit(Times expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.MUL, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(UnaryMinus expression) {
        WrapperIRExpression expr = expression.getExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.MINUS, new CONST(0), expr.unEx()));
    }

    @Override
    public WrapperIRExpression visit(VariableDefinition statement) {
        WrapperIRExpression irInitExpression = (statement.getInitExpression() != null ? statement.getInitExpression().visit(this) : new Ex(new CONST(0)));

        if (isFieldDefinition) {
            if (isStaticFieldDefinition) {
                // This code is generated for the method '_<class name>_init_static_fields'.
                if (mBodyMethodInitStaticFields == null) {
                    mLastStatementBodyMethodInitStaticFields = new SEQ();
                    mBodyMethodInitStaticFields = mLastStatementBodyMethodInitStaticFields;
                } else {
                    SEQ newSeq = new SEQ();
                    mLastStatementBodyMethodInitStaticFields.setRightStatement(newSeq);
                    mLastStatementBodyMethodInitStaticFields = newSeq;
                }

                mLastStatementBodyMethodInitStaticFields.setLeftStatement(new MOVE(new MEM(new BINOP(TypeBinOp.PLUS, new NAME(mClassDescriptionLbl), new CONST((mClassLayout.get(mCurrentFieldId) + 1) * mCurrentFrame.getWordSize()))), irInitExpression.unEx()));
            } else {
                // This code is generated for the method '_<class name>_init_fields'.
                if (mBodyMethodInitFields == null) {
                    mLastStatementBodyMethodInitFields = new SEQ();
                    mBodyMethodInitFields = mLastStatementBodyMethodInitFields;
                } else {
                    SEQ newSeq = new SEQ();
                    mLastStatementBodyMethodInitFields.setRightStatement(newSeq);
                    mLastStatementBodyMethodInitFields = newSeq;
                }

                mLastStatementBodyMethodInitFields.setLeftStatement(new MOVE(new MEM(new BINOP(TypeBinOp.PLUS, mFirstArgInFunction.exp(new TEMP(mCurrentFrame.getFP())), new CONST((mObjectLayout.get(mCurrentFieldId) + 2) * mCurrentFrame.getWordSize()))), irInitExpression.unEx()));
            }
        } else {
            LocalVariableSymbol symbol = ((LocalVariableSymbol) statement.getSymbol());
            if (!symbol.isFormalArg()) {
                Access access = mCurrentFrame.allocLocal(true);
                mListLocalVars.put(symbol.getVarId(), access);
                return new Nx(new MOVE(access.exp(new TEMP(mCurrentFrame.getFP())), irInitExpression.unEx()));
            }
        }

        return null;
    }

    @Override
    public WrapperIRExpression visit(While statement) {
        Label lblEnd = Label.newLabel();
        Label lblBeginLoop = Label.newLabel();

        Label savedLblEnd = mCurrentLblEnd;
        mCurrentLblEnd = lblEnd;

        Label saveLblBeginLoop = mCurrentLblBeginLoop;
        mCurrentLblBeginLoop = lblBeginLoop;

        WrapperIRExpression expression = statement.getExpression().visit(this);
        WrapperIRExpression body = statement.getStatement().visit(this);

        mCurrentLblEnd = savedLblEnd;

        mCurrentLblBeginLoop = saveLblBeginLoop;

        Label lblTrue = Label.newLabel();

        return new Nx(
                new SEQ(
                        new LABEL(lblBeginLoop),
                        new SEQ(
                                new CJUMP(TypeRelOp.EQ, expression.unEx(), new CONST(1), lblTrue, lblEnd),
                                new SEQ(
                                        new LABEL(lblTrue),
                                        new SEQ(
                                                body != null ? body.unNx() : new EXP(new CONST(0)),
                                                new SEQ(
                                                        new JUMP(lblBeginLoop),
                                                        new LABEL(lblEnd)
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private LinkedList<IRExpression> args(IRExpression... irExpressions) {
        LinkedList<IRExpression> expressions = new LinkedList<>();

        for (IRExpression expression : irExpressions) {
            expressions.add(expression);
        }

        return expressions;
    }

    private LinkedList<IRExpression> args(LinkedList<Expression> listExpressions) {
        return args(null, listExpressions);
    }

    private LinkedList<IRExpression> args(IRExpression firstArg, LinkedList<Expression> listExpressions) {
        LinkedList<IRExpression> expressions = new LinkedList<>();

        if (firstArg != null) {
            expressions.add(firstArg);
        }

        for (Expression expression : listExpressions) {
            expressions.add(expression.visit(this).unEx());
        }

        return expressions;
    }

    private LinkedList<Boolean> args(Boolean... boolExpressions) {
        LinkedList<Boolean> expressions = new LinkedList<>();

        for (Boolean expression : boolExpressions) {
            expressions.add(expression);
        }

        return expressions;
    }

}
