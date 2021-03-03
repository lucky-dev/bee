package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.translate.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class If extends Statement {

    private Expression mExpression;
    private Statement mThenStatement;
    private Statement mElseStatement;

    public If(Expression expression, Statement thenStatement, Statement elseStatement) {
        mExpression = expression;
        mThenStatement = thenStatement;
        mElseStatement = elseStatement;
    }

    public Expression getExpression() {
        return mExpression;
    }

    public Statement getThenStatement() {
        return mThenStatement;
    }

    public Statement getElseStatement() {
        return mElseStatement;
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
