package bee.lang.ir.tree;

import java.util.LinkedList;

public class ESEQ extends IRExpression {

    private IRStatement mStatement;
    private IRExpression mExpression;

    public ESEQ(IRStatement statement, IRExpression expression) {
        mStatement = statement;
        mExpression = expression;
    }

    public IRStatement getStatement() {
        return mStatement;
    }

    public IRExpression getExpression() {
        return mExpression;
    }

    @Override
    public String toString() {
        return "ESEQ(" + mStatement + ", " + mExpression + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        return null;
    }

    @Override
    public IRExpression build(LinkedList<IRExpression> kids) {
        return null;
    }

}
