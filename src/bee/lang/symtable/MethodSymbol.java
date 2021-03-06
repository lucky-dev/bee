package bee.lang.symtable;

import bee.lang.ast.AccessModifier;
import bee.lang.ast.Identifier;
import bee.lang.ast.types.BaseType;

public class MethodSymbol extends BaseScope implements Symbol {

    private AccessModifier mAccessModifier;
    private boolean isStatic;
    private Identifier mIdentifier;
    private BaseType mType;
    private BaseScope mScope;
    private Symbol mNextSymbol;
    private int mId;
    private String mClassName;
    private String mMethodName;

    public MethodSymbol(AccessModifier accessModifier, boolean isStatic, Identifier identifier, BaseScope enclosingScope, String className, String methodName) {
        super(enclosingScope, enclosingScope.getScopeName() + "." + (identifier == null ? "constructor" : identifier.getName()));
        mAccessModifier = accessModifier;
        this.isStatic = isStatic;
        mIdentifier = identifier;
        mId = 0;
        mClassName = className;
        mMethodName = methodName;
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

    public boolean isConstructor() {
        return mIdentifier == null;
    }

    public String getMethodId() {
        return mClassName + "_method_" + mMethodName + "_" + mId + "";
    }

    @Override
    public Identifier getIdentifier() {
        return mIdentifier;
    }

    public boolean isStatic() {
        return isStatic;
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
        return mNextSymbol;
    }

    @Override
    public void setNextSymbol(Symbol nextSymbol) {
        mNextSymbol = nextSymbol;

        if (mNextSymbol != null) {
            mId = ((MethodSymbol) mNextSymbol).mId + 1;
        }
    }

}
