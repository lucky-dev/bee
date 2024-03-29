package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.lexer.Token;
import bee.lang.translate.ir.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class While extends Statement {

    private Expression mExpression;
    private Statement mStatement;
    private Token mToken;

    public While(Expression expression, Statement statement, Token token) {
        mExpression = expression;
        mStatement = statement;
        mToken = token;
    }

    public Expression getExpression() {
        return mExpression;
    }

    public Statement getStatement() {
        return mStatement;
    }

    public Token getToken() {
        return mToken;
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
