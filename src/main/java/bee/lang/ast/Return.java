package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.lexer.Token;
import bee.lang.translate.ir.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class Return extends Statement {

    private Token mToken;
    private Expression mExpression;

    public Return(Token token) {
        this(token, null);
    }

    public Return(Token token, Expression expression) {
        mToken = token;
        mExpression = expression;
    }

    public Token getToken() {
        return mToken;
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

    @Override
    public WrapperIRExpression visit(IRTreeVisitor visitor) {
        return visitor.visit(this);
    }

}
