package bee.lang.ir.tree;

import java.util.LinkedList;

public class MEM extends IRExpression {

    private IRExpression mExpression;

    public MEM(IRExpression expression) {
        mExpression = expression;
    }

    public IRExpression getExpression() {
        return mExpression;
    }

    @Override
    public String toString() {
        return "MEM(" + mExpression.toString() + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        LinkedList<IRExpression> kids = new LinkedList<>();
        kids.add(mExpression);
        return kids;
    }

    @Override
    public IRExpression build(LinkedList<IRExpression> kids) {
        return new MEM(kids.getFirst());
    }

}
