package bee.lang.ir.tree;

public enum TypeRelOp {

    EQ("=="),
    NE("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">=");

    private String mName;

    TypeRelOp(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return mName;
    }

}
