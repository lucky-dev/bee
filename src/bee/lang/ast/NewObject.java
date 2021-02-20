package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class NewObject extends Expression {

    private BaseType mType;
    private ArgumentsList mArgumentsList;

    public NewObject(BaseType type, ArgumentsList argumentsList) {
        mType = type;
        mArgumentsList = argumentsList;
    }

    public BaseType getType() {
        return mType;
    }

    public ArgumentsList getArgumentsList() {
        return mArgumentsList;
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
