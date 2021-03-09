package bee.lang.translate;

import bee.lang.ast.*;
import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.*;
import bee.lang.symtable.ClassSymbol;
import bee.lang.symtable.MethodSymbol;
import bee.lang.translate.frame.Frame;
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
public class NewIRTreeVisitor implements IRTreeVisitor {

    private Label mCurrentLblEnd;
    private Label mCurrentLblBeginLoop;
    private Frame mFrame;
    private HashMap<String, EntityLayout> mObjectLayouts;
    private HashMap<String, EntityLayout> mClassLayouts;
    private HashMap<String, EntityLayout> mMethodLayouts;
    private ClassSymbol mCurrentClassSymbol;
    // The list of fragments keeps all procedures and data.
    private LinkedList<Fragment> mListFragments;

    public NewIRTreeVisitor(Frame frame,
                            HashMap<String, EntityLayout> objectLayouts,
                            HashMap<String, EntityLayout> classLayouts,
                            HashMap<String, EntityLayout> methodLayouts) {
        mFrame = frame;
        mObjectLayouts = objectLayouts;
        mClassLayouts = classLayouts;
        mMethodLayouts = methodLayouts;
        mListFragments = new LinkedList<>();
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

        return new Ex(new MEM(new BINOP(TypeBinOp.PLUS, new MEM(arrayExpression.unEx()), new BINOP(TypeBinOp.MUL, arrayIndex.unEx(), new CONST(mFrame.getWordSize())))));
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

        LinkedList<IRExpression> args = new LinkedList<>();

        for (Expression expr : expression.getArgumentsList().getExpressionList()) {
            args.add(expr.visit(this).unEx());
        }

        if (methodSymbol.isStatic()) {
            return new Ex(new CALL(new NAME(Label.newLabel(methodSymbol.getMethodId())), args));
        } else {
            // The first argument is the current object aka 'this'.
            IRExpression currentObject = expression.getExpression().visit(this).unEx();
            args.addFirst(currentObject);

            if ((methodSymbol.isPrivate()) || (expression.getExpression() instanceof Super)) {
                return new Ex(new CALL(new NAME(Label.newLabel(methodSymbol.getMethodId())), args));
            } else {
                EntityLayout virtualTable = mMethodLayouts.get(mCurrentClassSymbol.getIdentifier().getName());
                int virtualMethodId = virtualTable.get(((MethodSymbol) expression.getSymbol()).getMethodId());
                return new Ex(new CALL(new BINOP(TypeBinOp.PLUS, new MEM(new MEM(currentObject)), new BINOP(TypeBinOp.MUL, new CONST(virtualMethodId), new CONST(mFrame.getWordSize()))), args));
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

        statement.getFieldDefinitions().visit(this);
        statement.getConstructorDefinitions().visit(this);
        statement.getMethodDefinitions().visit(this);

        mCurrentClassSymbol = null;

        return null;
    }

    @Override
    public WrapperIRExpression visit(ConstructorDefinition statement) {
        String methodName = ((MethodSymbol) statement.getSymbol()).getMethodId();

        Iterator<Statement> listIterator = statement.getFormalArgumentsList().getStatementsList().iterator();
        LinkedList<Boolean> args = new LinkedList<>();
        while (listIterator.hasNext()) {
            args.add(false);
        }

        WrapperIRExpression tree = statement.getBody().visit(this);

        mListFragments.add(new ProcedureFragment(tree.unNx(), mFrame.newFrame(Label.newLabel(methodName), args)));

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
                                        body.unNx()
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
        return null;
    }

    @Override
    public WrapperIRExpression visit(FieldDefinition statement) {
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
                                            thenStatement.unNx(),
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
                                            thenStatement.unNx(),
                                            new SEQ(
                                                    new JUMP(lblEnd),
                                                    new SEQ(
                                                            new LABEL(lblFalse),
                                                            new SEQ(
                                                                    elseStatement.unNx(),
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

        Iterator<Statement> listIterator = statement.getFormalArgumentsList().getStatementsList().iterator();
        LinkedList<Boolean> args = new LinkedList<>();
        while (listIterator.hasNext()) {
            args.add(false);
        }

        mListFragments.add(new ProcedureFragment(statement.getBody().visit(this).unNx(), mFrame.newFrame(Label.newLabel(methodName), args)));

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
        return null;
    }

    @Override
    public WrapperIRExpression visit(NewObject expression) {
        return null;
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
        return null;
    }

    @Override
    public WrapperIRExpression visit(Statements statement) {
        if (statement.getStatementsList().size() > 0) {
            if (statement.getStatementsList().size() > 1) {
                Iterator<Statement> iterator = statement.getStatementsList().iterator();

                SEQ listSeqs = new SEQ();
                SEQ currentSeq = listSeqs;
                int i = 0;
                while (iterator.hasNext()) {
                    currentSeq.setLeftStatement(iterator.next().visit(this).unNx());

                    if (i < statement.getStatementsList().size() - 2) {
                        SEQ nextSeq = new SEQ();
                        currentSeq.setRightStatement(nextSeq);
                        currentSeq = nextSeq;
                    } else {
                        currentSeq.setRightStatement(iterator.next().visit(this).unNx());
                    }

                    i++;
                }

                return new Nx(listSeqs);
            } else {
                return statement.getStatementsList().getFirst().visit(this);
            }
        }

        return new Nx(null);
    }

    @Override
    public WrapperIRExpression visit(StringLiteral expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(Subtract expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.MINUS, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(Super expression) {
        return new Ex(new TEMP(mFrame.getFirstArg()));
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
        return new Ex(new TEMP(mFrame.getFirstArg()));
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
                                                body.unNx(),
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

}
