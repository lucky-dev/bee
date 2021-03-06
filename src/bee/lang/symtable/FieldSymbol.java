package bee.lang.symtable;

import bee.lang.ast.AccessModifier;
import bee.lang.ast.Identifier;
import bee.lang.ast.types.BaseType;

public class FieldSymbol implements Symbol {

    private AccessModifier mAccessModifier;
    private boolean isStatic;
    private boolean isConst;
    private Identifier mIdentifier;
    private BaseType mType;
    private BaseScope mScope;
    private String mClassName;

    public FieldSymbol(AccessModifier accessModifier, boolean isStatic, boolean isConst, Identifier identifier, BaseType type, String className) {
        mAccessModifier = accessModifier;
        this.isStatic = isStatic;
        this.isConst = isConst;
        mIdentifier = identifier;
        mType = type;
        mClassName = className;
    }

    public AccessModifier getAccessModifier() {
        return mAccessModifier;
    }

    public boolean isPublic() {
        return mAccessModifier == AccessModifier.PUBLIC;
    }

    public boolean isProtected() {
        return mAccessModifier == AccessModifier.PROTECTED;
    }

    public boolean isPrivate() {
        return mAccessModifier == AccessModifier.PRIVATE;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isConst() {
        return isConst;
    }

    public String getFieldId() {
        return mClassName + "_field_" + mIdentifier.getName();
    }

    @Override
    public Identifier getIdentifier() {
        return mIdentifier;
    }

    @Override
    public void setType(BaseType type) {
        mType = type;
    }

    @Override
    public BaseType getType() {
        return mType;
    }

    @Override
    public BaseScope getScope() {
        return mScope;
    }

    @Override
    public void setScope(BaseScope scope) {
        mScope = scope;
    }

    @Override
    public Symbol getNextSymbol() {
        return null;
    }

    @Override
    public void setNextSymbol(Symbol nextSymbol) {
    }

}
