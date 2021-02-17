package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class Return extends Statement {

    private Expression mExpression;

    public Return() {
        this(null);
    }

    public Return(Expression expression) {
        mExpression = expression;
    }

    public Expression getExpression() {
        return mExpression;
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
