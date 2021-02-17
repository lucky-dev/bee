package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class ClassDefinition extends Statement {

    private Identifier mClassIdentifier;
    private Identifier mBaseClassIdentifier;
    private Statements mConstructorDefinitions;
    private Statements mMethodDefinitions;
    private Statements mFieldDefinitions;

    public ClassDefinition(Identifier baseClassIdentifier, Identifier classIdentifier, Statements constructorDefinitions, Statements methodDefinitions, Statements fieldDefinitions) {
        mBaseClassIdentifier = baseClassIdentifier;
        mClassIdentifier = classIdentifier;
        mConstructorDefinitions = constructorDefinitions;
        mMethodDefinitions = methodDefinitions;
        mFieldDefinitions = fieldDefinitions;
    }

    public Identifier getClassIdentifier() {
        return mClassIdentifier;
    }

    public Identifier getBaseClassIdentifier() {
        return mBaseClassIdentifier;
    }

    public Statements getConstructorDefinitions() {
        return mConstructorDefinitions;
    }

    public Statements getMethodDefinitions() {
        return mMethodDefinitions;
    }

    public Statements getFieldDefinitions() {
        return mFieldDefinitions;
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
