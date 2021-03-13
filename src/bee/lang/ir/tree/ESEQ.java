package bee.lang.ir.tree;

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

}
