package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class While extends Statement {

    private Expression mExpression;
    private Statement mStatement;

    public While(Expression expression, Statement statement) {
        mExpression = expression;
        mStatement = statement;
    }

    public Expression getExpression() {
        return mExpression;
    }

    public Statement getStatement() {
        return mStatement;
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
