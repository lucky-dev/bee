package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.lexer.Token;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class GreaterThan extends Expression {

    private Token mToken;
    private Expression mLeftExpression;
    private Expression mRightExpression;

    public GreaterThan(Token token, Expression leftExpression, Expression rightExpression) {
        mToken = token;
        mLeftExpression = leftExpression;
        mRightExpression = rightExpression;
    }

    public Token getToken() {
        return mToken;
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
