package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.lexer.Token;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class TernaryOperator extends Expression {

    private Token mToken;
    private Expression mConditionalExpression;
    private Expression mThenExpression;
    private Expression mElseExpression;

    public TernaryOperator(Token token, Expression conditionalExpression, Expression thenExpression, Expression elseExpression) {
        mToken = token;
        mConditionalExpression = conditionalExpression;
        mThenExpression = thenExpression;
        mElseExpression = elseExpression;
    }

    public Token getToken() {
        return mToken;
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
