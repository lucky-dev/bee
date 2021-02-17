package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class VariableDefinition extends Statement {

    private boolean isConst;
    private Identifier mIdentifier;
    private BaseType mType;
    private Expression mInitExpression;

    public VariableDefinition(boolean isConst, Identifier identifier, BaseType type, Expression initExpression) {
        this.isConst = isConst;
        mIdentifier = identifier;
        mType = type;
        mInitExpression = initExpression;
    }

    public boolean isConst() {
        return isConst;
    }

    public Identifier getIdentifier() {
        return mIdentifier;
    }

    public Expression getInitExpression() {
        return mInitExpression;
    }

    public void setInitExpression(Expression initExpression) {
        mInitExpression = initExpression;
    }

    public BaseType getType() {
        return mType;
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
