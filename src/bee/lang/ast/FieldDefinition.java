package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class FieldDefinition extends Statement {

    private AccessModifier mAccessModifier;
    private boolean isStatic;
    private VariableDefinition mVariableDefinition;

    public FieldDefinition(AccessModifier accessModifier, boolean isStatic, VariableDefinition variableDefinition) {
        mAccessModifier = accessModifier;
        this.isStatic = isStatic;
        mVariableDefinition = variableDefinition;
    }

    public AccessModifier getAccessModifier() {
        return mAccessModifier;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public VariableDefinition getVariableDefinition() {
        return mVariableDefinition;
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
