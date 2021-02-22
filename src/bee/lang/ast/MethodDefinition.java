package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class MethodDefinition extends Statement {

    private boolean isStatic;
    private AccessModifier mAccessModifier;
    private Identifier mIdentifier;
    private Statements mFormalArgumentsList;
    private BaseType mReturnType;
    private Statements mBody;

    public MethodDefinition(AccessModifier accessModifier, boolean isStatic, Identifier identifier, Statements formalArgumentsList, BaseType returnType, Statements body) {
        this.isStatic = isStatic;
        mAccessModifier = accessModifier;
        mIdentifier = identifier;
        mFormalArgumentsList = formalArgumentsList;
        mReturnType = returnType;
        mBody = body;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public AccessModifier getAccessModifier() {
        return mAccessModifier;
    }

    public Identifier getIdentifier() {
        return mIdentifier;
    }

    public Statements getFormalArgumentsList() {
        return mFormalArgumentsList;
    }

    public BaseType getReturnType() {
        return mReturnType;
    }

    public Statements getBody() {
        return mBody;
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