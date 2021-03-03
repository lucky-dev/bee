package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.lexer.Token;
import bee.lang.translate.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class ArrayAccess extends Expression {

    private Token mToken;
    private Expression mExpression;
    private Expression mIndex;

    public ArrayAccess(Token token, Expression expression, Expression index) {
        mToken = token;
        mExpression = expression;
        mIndex = index;
    }

    public Token getToken() {
        return mToken;
    }

    public Expression getExpression() {
        return mExpression;
    }

    public Expression getIndex() {
        return mIndex;
    }

    @Override
    public BaseType visit(TypeVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public void visit(BaseVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public WrapperIRExpression visit(IRTreeVisitor visitor) {
        return visitor.visit(this);
    }

}
