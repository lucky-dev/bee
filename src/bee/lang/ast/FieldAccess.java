package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.translate.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class FieldAccess extends Expression {

    private Expression mExpression;
    private Identifier mIdentifier;

    public FieldAccess(Expression expression, Identifier identifier) {
        mExpression = expression;
        mIdentifier = identifier;
    }

    public Expression getExpression() {
        return mExpression;
    }

    public Identifier getIdentifier() {
        return mIdentifier;
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
