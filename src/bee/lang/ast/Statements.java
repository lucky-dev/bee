package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

import java.util.LinkedList;

public class Statements extends Statement {

    private LinkedList<Statement> mStatementsList;

    public Statements() {
        mStatementsList = new LinkedList<>();
    }

    public void addStatement(Statement statement) {
        mStatementsList.add(statement);
    }

    public LinkedList<Statement> getStatementsList() {
        return mStatementsList;
    }

    @Override
    public BaseType visit(TypeVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public void visit(BaseVisitor visitor) {
        visitor.visit(this);
    }

}
