package bee.lang.ir.tree;

public class MEM extends IRExpression {

    private IRExpression mExpression;

    public MEM(IRExpression expression) {
        mExpression = expression;
    }

    @Override
    public String toString() {
        return "MEM(" + mExpression.toString() + ")";
    }

}
