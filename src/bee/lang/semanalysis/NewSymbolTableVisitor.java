package bee.lang.semanalysis;

import bee.lang.ast.*;
import bee.lang.ast.types.ClassType;
import bee.lang.ast.types.MethodType;
import bee.lang.ast.types.Type;
import bee.lang.lexer.Token;
import bee.lang.symtable.*;
import bee.lang.visitors.BaseVisitor;

import java.util.*;

// This class creates a new symbol table. Symbol table has such format: Global scope <- Base class scope <- Subclass scope <- Method scope <- Local (block) scope.
// Global scope contains names of all classes.
// Class scope contains names of all methods and fields.
// Method scope and local (block) scope contain names of local variables.
public class NewSymbolTableVisitor implements BaseVisitor {

    private BaseScope mCurrentScope;
    private ClassSymbol mCurrentClassSymbol;
    private MethodSymbol mCurrentMethodSymbol;
    private int mCountVars;
    private LinkedList<String> mSortedListOfClasses;

    public NewSymbolTableVisitor() {
        mCurrentScope = new GlobalScope(null);
        mSortedListOfClasses = new LinkedList<>();
    }

    public BaseScope getCurrentScope() {
        return mCurrentScope;
    }

    public LinkedList<String> getSortedListOfClasses() {
        return mSortedListOfClasses;
    }

    @Override
    public void visit(Add expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(And expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(ArrayAccess expression) {
        expression.getExpression().visit(this);
        expression.getIndex().visit(this);
    }

    @Override
    public void visit(Assignment expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(AssignmentStatement statement) {
        statement.getExpression().visit(this);
    }

    @Override
    public void visit(Block statement) {
        mCurrentScope = new LocalScope(mCurrentScope, mCurrentScope.getScopeName());

        statement.getStatements().visit(this);

        mCurrentScope = mCurrentScope.getEnclosingScope();
    }

    @Override
    public void visit(BoolLiteral expression) {
    }

    @Override
    public void visit(Break statement) {
    }

    @Override
    public void visit(Continue statement) {
    }

    @Override
    public void visit(Call expression) {
        expression.getExpression().visit(this);

        Iterator<Expression> iterator = expression.getArgumentsList().getExpressionList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }
    }

    @Override
    public void visit(CharLiteral expression) {
    }

    @Override
    public void visit(ClassDefinition statement) {
        Symbol symbolClass = mCurrentScope.getSymbolInCurrentScope(statement.getClassIdentifier().getName());

        if (symbolClass == null) {
            BaseScope baseScope = mCurrentScope;

            if (statement.getBaseClassIdentifier() != null) {
                baseScope = (ClassSymbol) mCurrentScope.getSymbolInCurrentScope(statement.getBaseClassIdentifier().getName());
            }

            ClassSymbol newClassSymbol = new ClassSymbol(statement.getClassIdentifier(), statement.getBaseClassIdentifier(), baseScope);

            mCurrentScope.put(newClassSymbol);

            statement.setSymbol(newClassSymbol);

            mCurrentScope = newClassSymbol;

            mCurrentClassSymbol = newClassSymbol;

            statement.getFieldDefinitions().visit(this);
            statement.getConstructorDefinitions().visit(this);
            statement.getMethodDefinitions().visit(this);

            mCurrentClassSymbol = null;

            mCurrentScope = newClassSymbol.getScope();
        } else {
            printErrorMessage(statement.getClassIdentifier().getToken(), "Class '" + statement.getClassIdentifier().getName() + "' is already defined.");
        }
    }

    @Override
    public void visit(ConstructorDefinition statement) {
        MethodSymbol newMethodSymbol = new MethodSymbol(statement.getAccessModifier(), false, null, mCurrentScope, mCurrentClassSymbol.getIdentifier().getName(), "constructor");

        MethodType methodType = new MethodType();

        Iterator<Statement> iterator = statement.getFormalArgumentsList().getStatementsList().iterator();

        while (iterator.hasNext()) {
            VariableDefinition variableDefinition = (VariableDefinition) iterator.next();
            methodType.addFormalArgumentType(variableDefinition.getType());
        }

        methodType.addReturnType(Type.Class(((ClassSymbol) mCurrentScope).getIdentifier()));

        newMethodSymbol.setType(methodType);

        newMethodSymbol.setNextSymbol(mCurrentScope.getSymbolInCurrentScope("constructor"));
        mCurrentScope.put("constructor", newMethodSymbol);

        statement.setSymbol(newMethodSymbol);

        mCurrentScope = newMethodSymbol;

        mCurrentMethodSymbol = newMethodSymbol;

        mCountVars = 0;

        statement.getFormalArgumentsList().visit(this);
        statement.getBody().visit(this);

        mCurrentMethodSymbol = null;

        mCurrentScope = mCurrentScope.getEnclosingScope();
    }

    @Override
    public void visit(Div expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(DoWhile statement) {
        statement.getExpression().visit(this);
        statement.getStatement().visit(this);
    }

    @Override
    public void visit(Equal expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(FieldAccess expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visit(FieldDefinition statement) {
        VariableDefinition variableDefinition = statement.getVariableDefinition();

        Symbol symbol = mCurrentScope.getSymbolInCurrentScope(variableDefinition.getIdentifier().getName());

        if (symbol == null) {
            FieldSymbol fieldSymbol = new FieldSymbol(statement.getAccessModifier(), statement.isStatic(), variableDefinition.isConst(), variableDefinition.getIdentifier(), variableDefinition.getType(), mCurrentClassSymbol.getIdentifier().getName());
            mCurrentScope.put(fieldSymbol);
            statement.setSymbol(fieldSymbol);
        } else {
            printErrorMessage(statement.getVariableDefinition().getIdentifier().getToken(), mCurrentScope.getScopeName() + " already has the identifier '" + statement.getVariableDefinition().getIdentifier().getName() + "'.");
        }
    }

    @Override
    public void visit(GreaterEqualThan expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(GreaterThan expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(Identifier expression) {
        if (mCurrentScope.getSymbol(expression.getName()) == null) {
            printErrorMessage(expression.getToken(), mCurrentScope.getScopeName() + " has no the identifier '" + expression.getName() + "'.");
        }
    }

    @Override
    public void visit(If statement) {
        statement.getExpression().visit(this);
        statement.getThenStatement().visit(this);
        if (statement.getElseStatement() != null) {
            statement.getElseStatement().visit(this);
        }
    }

    @Override
    public void visit(IntLiteral expression) {
    }

    @Override
    public void visit(LessEqualThan expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(LessThan expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(MethodDefinition statement) {
        Symbol symbol = mCurrentScope.getSymbolInCurrentScope(statement.getIdentifier().getName());

        if ((symbol != null) && (!(symbol instanceof MethodSymbol))) {
            printErrorMessage(statement.getIdentifier().getToken(), "'" + mCurrentScope + "' has already had the identifier '" + statement.getIdentifier().getName() + "'.");
            return;
        }

        MethodSymbol newMethodSymbol = new MethodSymbol(statement.getAccessModifier(), statement.isStatic(), statement.getIdentifier(), mCurrentScope, mCurrentClassSymbol.getIdentifier().getName(), statement.getIdentifier().getName());

        MethodType methodType = new MethodType();

        Iterator<Statement> iterator = statement.getFormalArgumentsList().getStatementsList().iterator();

        while (iterator.hasNext()) {
            methodType.addFormalArgumentType(((VariableDefinition) iterator.next()).getType());
        }

        methodType.addReturnType(statement.getReturnType());

        newMethodSymbol.setType(methodType);

        newMethodSymbol.setNextSymbol(mCurrentScope.getSymbolInCurrentScope(statement.getIdentifier().getName()));
        mCurrentScope.put(newMethodSymbol);

        statement.setSymbol(newMethodSymbol);

        mCurrentScope = newMethodSymbol;

        mCurrentMethodSymbol = newMethodSymbol;

        mCountVars = 0;

        statement.getFormalArgumentsList().visit(this);
        statement.getBody().visit(this);

        mCurrentMethodSymbol = null;

        mCurrentScope = mCurrentScope.getEnclosingScope();
    }

    @Override
    public void visit(Mod expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(NewArray expression) {
        if (expression.getBaseArray() != null) {
            expression.getBaseArray().visit(this);
        }

        expression.getSize().visit(this);
    }

    @Override
    public void visit(NewObject expression) {
        Iterator<Expression> iterator = expression.getArgumentsList().getExpressionList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }
    }

    @Override
    public void visit(Nil expression) {
    }

    @Override
    public void visit(Not expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visit(NotEqual expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(Or expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(Program statement) {
        HashMap<String, ClassDefinition> allClasses = new HashMap<>();
        // Store the list of classes in format "ClassName" -> ID, e.g. "Animal" -> 1
        HashMap<String, Integer> classIds = new HashMap<>();
        // Store the list of classes in format ID -> "ClassName", e.g. 1 -> "Animal"
        HashMap<Integer, String> invertedClassIds = new HashMap<>();

        // Initialize graph of classes. Use format for graph "Class ID" -> [ "Subclass ID" ].
        // E.g. 1 -> [ 2, 3 ] ("Animal" = 1, "Cat" = 2, "Dog" = 3).
        // The root class of all classes has id `0` by default, but the language Bee does not support the default root class for all classes.
        ArrayList<HashSet<Integer>> graphOfClasses = new ArrayList<>();
        for (int i = 0; i < statement.getStatementsList().size() + 1; i++) {
            graphOfClasses.add(new HashSet<>());
        }

        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        // Class id `0` is reserved as root class (stub class) for all classes.
        int classId = 1;

        while (iterator.hasNext()) {
            ClassDefinition classDefinition = (ClassDefinition) iterator.next();

            allClasses.put(classDefinition.getClassIdentifier().getName(), classDefinition);

            ClassType classType = Type.Class(classDefinition.getClassIdentifier());

            if (classType == null) {
                classType = Type.defineClassType(classDefinition.getClassIdentifier());
            }

            String className = classDefinition.getClassIdentifier().getName();

            if (!classIds.containsKey(className)) {
                classIds.put(className, classId);
                invertedClassIds.put(classId, className);
                classId++;
            }

            if (classDefinition.getBaseClassIdentifier() != null) {
                ClassType baseClassType = Type.Class(classDefinition.getBaseClassIdentifier());

                if (baseClassType == null) {
                    baseClassType = Type.defineClassType(classDefinition.getBaseClassIdentifier());
                }

                String baseClassName = classDefinition.getBaseClassIdentifier().getName();

                if (!classIds.containsKey(baseClassName)) {
                    classIds.put(baseClassName, classId);
                    invertedClassIds.put(classId, baseClassName);
                    classId++;
                }

                classType.setBaseClass(baseClassType);

                graphOfClasses.get(classIds.get(baseClassName)).add(classIds.get(className));
            } else {
                graphOfClasses.get(0).add(classIds.get(className));
            }
        }

        for (String name : Type.getDefinedClassesNames()) {
            if (!allClasses.containsKey(name)) {
                printErrorMessage(Type.Class(name).getIdentifier().getToken(), "Class '" + name + "' is not found.");
                return;
            }
        }

        // Do topological sort of classes. Need to parse base classes first. Then need to parse subclasses.
        // Topological sorting starts from root class with id `0`. If the graph contains cycles the current sorting algorithm does not reach these classes.
        LinkedList<Integer> sortedClassIds = topologicalSort(graphOfClasses);

        if (sortedClassIds.size() == graphOfClasses.size()) {
            // Remove first class with id `0`. It is a stub.
            sortedClassIds.removeFirst();

            Iterator<Integer> iteratorClassIds = sortedClassIds.iterator();

            while (iteratorClassIds.hasNext()) {
                mSortedListOfClasses.add(invertedClassIds.get(iteratorClassIds.next()));
            }

            Iterator<String> sortedListOfClasses = mSortedListOfClasses.iterator();
            while (sortedListOfClasses.hasNext()) {
                allClasses.get(sortedListOfClasses.next()).visit(this);
            }
        } else {
            printErrorMessage(null, "There is a cycle in inherited classes.");
        }

        for (Map.Entry<String, ClassDefinition> item : allClasses.entrySet()) {
            if (item.getValue().getConstructorDefinitions().getStatementsList().isEmpty()) {
                printErrorMessage(item.getValue().getClassIdentifier().getToken(), "Class '" + item.getKey() + "' has no constructors. Class must have at least one constructor.");
            }
        }
    }

    private LinkedList<Integer> topologicalSort(ArrayList<HashSet<Integer>> graphOfClasses) {
        LinkedList<Integer> stackOfClassIds = new LinkedList<>();

        topologicalSortDfs(0, graphOfClasses, stackOfClassIds);

        return stackOfClassIds;
    }

    private void topologicalSortDfs(int classId, ArrayList<HashSet<Integer>> graphOfClasses, LinkedList<Integer> stack) {
        HashSet<Integer> setOfClasses = graphOfClasses.get(classId);

        for (Integer id : setOfClasses) {
            topologicalSortDfs(id, graphOfClasses, stack);
        }

        stack.push(classId);
    }

    @Override
    public void visit(Return statement) {
        if (statement.getExpression() != null) {
            statement.getExpression().visit(this);
        }
    }

    @Override
    public void visit(Statements statement) {
        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }
    }

    @Override
    public void visit(StringLiteral expression) {
    }

    @Override
    public void visit(Subtract expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(Super expression) {
    }

    @Override
    public void visit(TernaryOperator expression) {
        expression.getConditionalExpression().visit(this);
        expression.getThenExpression().visit(this);
        expression.getElseExpression().visit(this);
    }

    @Override
    public void visit(This expression) {
    }

    @Override
    public void visit(Times expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(UnaryMinus expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visit(VariableDefinition statement) {
        Symbol symbol = mCurrentScope.getSymbolInCurrentScope(statement.getIdentifier().getName());

        if (symbol == null) {
            mCurrentScope.put(new LocalVariableSymbol(statement.isConst(), statement.getIdentifier(), statement.getType(), mCurrentClassSymbol.getIdentifier().getName(), mCurrentMethodSymbol.getIdentifier() == null ? "constructor" : mCurrentMethodSymbol.getIdentifier().getName(), mCountVars++));
        } else {
            printErrorMessage(statement.getIdentifier().getToken(), mCurrentScope.getScopeName() + "' already has the identifier '" + statement.getIdentifier().getName() + "'.");
        }
    }

    @Override
    public void visit(While statement) {
        statement.getExpression().visit(this);
        statement.getStatement().visit(this);
    }

    private void printErrorMessage(Token token, String message) {
        System.out.println((token == null ? "" : "[ " + token.getFileName() + " : " + token.getLine() + " ] ") + message);
    }

}
