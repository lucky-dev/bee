package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class FieldAccess extends Expression {

    private Expression mExpressionParent;
    private Identifier mIdentifier;

    public FieldAccess(Expression expressionParent, Identifier identifier) {
        mExpressionParent = expressionParent;
        mIdentifier = identifier;
    }

    public Expression getExpressionParent() {
        return mExpressionParent;
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

}
