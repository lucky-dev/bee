package bee.lang.symtable;

import bee.lang.ast.Identifier;
import bee.lang.ast.types.BaseType;

public class LocalVariableSymbol implements Symbol {

    private boolean isConst;
    private Identifier mIdentifier;
    private BaseType mType;
    private BaseScope mScope;

    public LocalVariableSymbol(boolean isConst, Identifier identifier, BaseType type) {
        this.isConst = isConst;
        mIdentifier = identifier;
        mType = type;
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
