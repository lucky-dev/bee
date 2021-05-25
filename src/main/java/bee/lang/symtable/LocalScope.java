package bee.lang.symtable;

public class LocalScope extends BaseScope {

    public LocalScope(BaseScope enclosingScope, String scopeName) {
        super(enclosingScope, scopeName);
    }

}
