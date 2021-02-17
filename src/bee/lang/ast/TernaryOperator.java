package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class TernaryOperator extends Expression {

    private Expression mConditionalExpression;
    private Expression mThenExpression;
    private Expression mElseExpression;

    public TernaryOperator(Expression conditionalExpression, Expression thenExpression, Expression elseExpression) {
        mConditionalExpression = conditionalExpression;
        mThenExpression = thenExpression;
        mElseExpression = elseExpression;
    }

    public Expression getConditionalExpression() {
        return mConditionalExpression;
    }

    public Expression getThenExpression() {
        return mThenExpression;
    }

    public Expression getElseExpression() {
        return mElseExpression;
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
