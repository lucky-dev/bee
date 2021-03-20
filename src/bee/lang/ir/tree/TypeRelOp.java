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

    public TypeRelOp negate() {
        if (this == EQ) {
            return NE;
        }

        if (this == NE) {
            return EQ;
        }

        if (this == LT) {
            return GE;
        }

        if (this == GT) {
            return LE;
        }

        if (this == LE) {
            return GT;
        }

        if (this == GE) {
            return LT;
        }

        return null;
    }

    @Override
    public String toString() {
        return mName;
    }

}
