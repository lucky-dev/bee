package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.lexer.Token;
import bee.lang.translate.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class FieldDefinition extends Statement {

    private Token mToken;
    private AccessModifier mAccessModifier;
    private boolean isStatic;
    private VariableDefinition mVariableDefinition;

    public FieldDefinition(Token token, AccessModifier accessModifier, boolean isStatic, VariableDefinition variableDefinition) {
        mToken = token;
        mAccessModifier = accessModifier;
        this.isStatic = isStatic;
        mVariableDefinition = variableDefinition;
    }

    public Token getToken() {
        return mToken;
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

    @Override
    public WrapperIRExpression visit(IRTreeVisitor visitor) {
        return visitor.visit(this);
    }

}
