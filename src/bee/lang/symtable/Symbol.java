package bee.lang.symtable;

import bee.lang.ast.Identifier;
import bee.lang.ast.types.BaseType;

public interface Symbol {

    Identifier getIdentifier();
    void setType(BaseType type);
    BaseType getType();
    BaseScope getScope();
    void setScope(BaseScope scope);
    Symbol getNextSymbol();
    void setNextSymbol(Symbol nextSymbol);

}
