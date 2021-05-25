package bee.lang.ir.tree;

import java.util.LinkedList;

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

    @Override
    public LinkedList<IRExpression> kids() {
        return null;
    }

    @Override
    public IRExpression build(LinkedList<IRExpression> kids) {
        return this;
    }

}
