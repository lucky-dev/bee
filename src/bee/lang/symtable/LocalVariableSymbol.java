package bee.lang.symtable;

import bee.lang.ast.Identifier;
import bee.lang.ast.types.BaseType;

public class LocalVariableSymbol implements Symbol {

    private boolean isConst;
    private Identifier mIdentifier;
    private BaseType mType;
    private BaseScope mScope;
    private String mClassName;
    private String mMethodName;
    private int mId;
    private String mVarId;

    public LocalVariableSymbol(boolean isConst, Identifier identifier, BaseType type, String className, String methodName, int id) {
        this.isConst = isConst;
        mIdentifier = identifier;
        mType = type;
        mId = id;
        mClassName = className;
        mMethodName = methodName;
    }

    public boolean isConst() {
        return isConst;
    }

    public String getVarId() {
        return mClassName + "_" + mMethodName + "_localVar_" + mIdentifier.getName() + "_" + mId;
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
