package bee.lang.ir.tree;

public enum TypeBinOp {

    PLUS("+"),
    MINUS("-"),
    MUL("*"),
    DIV("/"),
    MOD("%");

    private String mName;

    TypeBinOp(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }

}
