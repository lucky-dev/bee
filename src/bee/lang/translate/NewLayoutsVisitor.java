package bee.lang.translate;

import bee.lang.ast.*;
import bee.lang.symtable.*;
import bee.lang.visitors.BaseVisitor;

import java.util.*;

public class NewLayoutsVisitor implements BaseVisitor {

    private BaseScope mCurrentScope;
    private ClassSymbol mCurrentClassSymbol;
    private MethodSymbol mCurrentMethodSymbol;
    private LinkedList<String> mSortedListOfClasses;

    public NewLayoutsVisitor(BaseScope baseScope, LinkedList<String> sortedListOfClasses) {
        mCurrentScope = baseScope;
        mSortedListOfClasses = sortedListOfClasses;
    }

    @Override
    public void visit(Add expression) {
    }

    @Override
    public void visit(And expression) {
    }

    @Override
    public void visit(ArrayAccess expression) {
    }

    @Override
    public void visit(Assignment expression) {
    }

    @Override
    public void visit(AssignmentStatement statement) {
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
    }

    @Override
    public void visit(CharLiteral expression) {
    }

    @Override
    public void visit(ClassDefinition statement) {
        mCurrentClassSymbol = (ClassSymbol) statement.getSymbol();

        mCurrentScope = mCurrentClassSymbol;

        statement.getFieldDefinitions().visit(this);
        statement.getConstructorDefinitions().visit(this);
        statement.getMethodDefinitions().visit(this);

        mCurrentScope = mCurrentClassSymbol.getScope();

        mCurrentClassSymbol = null;
    }

    @Override
    public void visit(ConstructorDefinition statement) {
        mCurrentMethodSymbol = (MethodSymbol) statement.getSymbol();

        mCurrentScope = mCurrentMethodSymbol;

        statement.getBody().visit(this);

        mCurrentMethodSymbol = null;

        mCurrentScope = mCurrentScope.getEnclosingScope();
    }

    @Override
    public void visit(Div expression) {
    }

    @Override
    public void visit(DoWhile statement) {
        statement.getStatement().visit(this);
    }

    @Override
    public void visit(Equal expression) {
    }

    @Override
    public void visit(FieldAccess expression) {
    }

    @Override
    public void visit(FieldDefinition statement) {
        // TODO Implement this
    }

    @Override
    public void visit(GreaterEqualThan expression) {
    }

    @Override
    public void visit(GreaterThan expression) {
    }

    @Override
    public void visit(Identifier expression) {
    }

    @Override
    public void visit(If statement) {
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
    }

    @Override
    public void visit(LessThan expression) {
    }

    @Override
    public void visit(MethodDefinition statement) {
        mCurrentMethodSymbol = (MethodSymbol) statement.getSymbol();

        // TODO Implement this

        mCurrentScope = mCurrentMethodSymbol;

        statement.getBody().visit(this);

        mCurrentMethodSymbol = null;

        mCurrentScope = mCurrentScope.getEnclosingScope();
    }

    @Override
    public void visit(Mod expression) {
    }

    @Override
    public void visit(NewArray expression) {
    }

    @Override
    public void visit(NewObject expression) {
    }

    @Override
    public void visit(Nil expression) {
    }

    @Override
    public void visit(Not expression) {
    }

    @Override
    public void visit(NotEqual expression) {
    }

    @Override
    public void visit(Or expression) {
    }

    @Override
    public void visit(Program statement) {
        HashMap<String, ClassDefinition> allClasses = new HashMap<>();

        Iterator<Statement> listOfClassesIterator = statement.getStatementsList().iterator();

        while (listOfClassesIterator.hasNext()) {
            ClassDefinition classDefinition = (ClassDefinition) listOfClassesIterator.next();
            allClasses.put(classDefinition.getClassIdentifier().getName(), classDefinition);
        }

        Iterator<String> sortedListOfClassesIterator = mSortedListOfClasses.iterator();

        while (sortedListOfClassesIterator.hasNext()) {
            allClasses.get(sortedListOfClassesIterator.next()).visit(this);
        }
    }

    @Override
    public void visit(Return statement) {
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
    }

    @Override
    public void visit(Super expression) {
    }

    @Override
    public void visit(TernaryOperator expression) {
    }

    @Override
    public void visit(This expression) {
    }

    @Override
    public void visit(Times expression) {
    }

    @Override
    public void visit(UnaryMinus expression) {
    }

    @Override
    public void visit(VariableDefinition statement) {
    }

    @Override
    public void visit(While statement) {
        statement.getStatement().visit(this);
    }

}
