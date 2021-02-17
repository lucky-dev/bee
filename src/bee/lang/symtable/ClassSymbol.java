package bee.lang.symtable;

import bee.lang.ast.Identifier;
import bee.lang.ast.types.BaseType;
import bee.lang.ast.types.ClassClassType;
import bee.lang.ast.types.Type;

public class ClassSymbol extends BaseScope implements Symbol {

    private Identifier mIdentifier;
    private Identifier mBaseClassIdentifier;
    private BaseScope mScope;
    private BaseType mClassClassType;

    public ClassSymbol(Identifier identifier, Identifier baseClassIdentifier, BaseScope baseScope) {
        super(baseScope, identifier.getName());
        mIdentifier = identifier;
        mBaseClassIdentifier = baseClassIdentifier;
        mClassClassType = new ClassClassType(Type.Class(identifier.getName()));
    }

    public Identifier getIdentifier() {
        return mIdentifier;
    }

    public Identifier getBaseClassIdentifier() {
        return mBaseClassIdentifier;
    }

    @Override
    public void setType(BaseType type) {
        mClassClassType = type;
    }

    public BaseType getType() {
        return mClassClassType;
    }

    public BaseScope getScope() {
        return mScope;
    }

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

    @Override
    public SymbolType getSymbolType() {
        return SymbolType.CLASS;
    }

}
