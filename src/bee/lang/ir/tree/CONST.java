package bee.lang.ir.tree;

public class CONST extends IRExpression {

    private int mValue;

    public CONST(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

}
