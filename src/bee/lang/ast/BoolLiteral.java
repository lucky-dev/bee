package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class BoolLiteral extends Expression {

    private boolean mValue;

    public BoolLiteral(boolean value) {
        mValue = value;
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
