package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.translate.ir.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class BoolLiteral extends Expression {

    private boolean mValue;

    public BoolLiteral(boolean value) {
        mValue = value;
    }

    public boolean getValue() {
        return mValue;
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
