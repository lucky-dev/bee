package bee.lang.ir.tree;

import java.util.LinkedList;

public class EXP extends IRStatement {

    private IRExpression mExpression;

    public EXP(IRExpression expression) {
        mExpression = expression;
    }

    public IRExpression getExpression() {
        return mExpression;
    }

    @Override
    public String toString() {
        return "EXP(" + mExpression + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        LinkedList<IRExpression> kids = new LinkedList<>();
        kids.add(mExpression);
        return kids;
    }

    @Override
    public IRStatement build(LinkedList<IRExpression> kids) {
        return new EXP(kids.getFirst());
    }

}
