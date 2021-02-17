package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class Call extends Expression {

    private Identifier mIdentifier;
    private ArgumentsList mArgumentsList;

    public Call(Identifier identifier, ArgumentsList argumentsList) {
        mIdentifier = identifier;
        mArgumentsList = argumentsList;
    }

    public Identifier getIdentifier() {
        return mIdentifier;
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
