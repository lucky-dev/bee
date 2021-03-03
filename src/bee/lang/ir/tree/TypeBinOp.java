package bee.lang.ir.tree;

public enum TypeBinOp {

    PLUS("+"),
    MINUS("-"),
    MUL("*"),
    DIV("/"),
    MOD("%"),
    AND("&&"),
    OR("||");

    private String mName;

    TypeBinOp(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }

}
