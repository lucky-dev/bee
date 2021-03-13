package bee.lang.ir.tree;

public class CONST extends IRExpression {

    private int mValue;

    public CONST(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    @Override
    public String toString() {
        return "CONST(" + mValue + ")";
    }

}
