package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class LessEqualThan extends Expression {

    private Expression mLeftExpression;
    private Expression mRightExpression;

    public LessEqualThan(Expression leftExpression, Expression rightExpression) {
        mLeftExpression = leftExpression;
        mRightExpression = rightExpression;
    }

    public Expression getLeftExpression() {
        return mLeftExpression;
    }

    public Expression getRightExpression() {
        return mRightExpression;
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
