package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.translate.ir.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class NewArray extends Expression {

    private BaseType mType;
    private Expression mSize;
    private NewArray mBaseArray;

    public NewArray(BaseType type, Expression size, NewArray baseArray) {
        mType = type;
        mSize = size;
        mBaseArray = baseArray;
    }

    public BaseType getType() {
        return mType;
    }

    public Expression getSize() {
        return mSize;
    }

    public NewArray getBaseArray() {
        return mBaseArray;
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
