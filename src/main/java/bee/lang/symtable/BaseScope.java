package bee.lang.symtable;

import java.util.HashMap;
import java.util.Set;

public abstract class BaseScope {

    private BaseScope mEnclosingScope;
    private HashMap<String, Symbol> mSymbols;
    private String mScopeName;

    public BaseScope(BaseScope enclosingScope, String scopeName) {
        mEnclosingScope = enclosingScope;
        mSymbols = new HashMap<>();
        mScopeName = scopeName;
    }

    public void put(Symbol symbol) {
        put(symbol.getIdentifier().getName(), symbol);
    }

    public void put(String name, Symbol symbol) {
        mSymbols.put(name, symbol);
        symbol.setScope(this);
    }

    public Symbol getSymbol(String name) {
        Symbol symbol = getSymbolInCurrentScope(name);

        if (symbol != null) {
            return symbol;
        }

        if (mEnclosingScope != null) {
            return mEnclosingScope.getSymbol(name);
        }

        return null;
    }

    public Symbol getSymbolInCurrentScope(String name) {
        return mSymbols.get(name);
    }

    public BaseScope getEnclosingScope() {
        return mEnclosingScope;
    }

    public void setEnclosingScope(BaseScope enclosingScope) {
        mEnclosingScope = enclosingScope;
    }

    public String getScopeName() {
        return mScopeName;
    }

    public Set<String> getKeys() {
        return mSymbols.keySet();
    }

    public String toString() {
        return mScopeName;
    }

}
