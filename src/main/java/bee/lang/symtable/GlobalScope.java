package bee.lang.symtable;

public class GlobalScope extends BaseScope {

    public GlobalScope(BaseScope enclosingScope) {
        super(enclosingScope, "global");
    }

}
