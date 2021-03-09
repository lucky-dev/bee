package bee.lang.ir.tree;

public class SEQ extends IRStatement {

    private IRStatement mLeftStatement;
    private IRStatement mRightStatement;

    public SEQ() {
        this(null, null);
    }

    public SEQ(IRStatement leftStatement, IRStatement rightStatement) {
        mLeftStatement = leftStatement;
        mRightStatement = rightStatement;
    }

    public IRStatement getLeftStatement() {
        return mLeftStatement;
    }

    public void setLeftStatement(IRStatement leftStatement) {
        mLeftStatement = leftStatement;
    }

    public IRStatement getRightStatement() {
        return mRightStatement;
    }

    public void setRightStatement(IRStatement rightStatement) {
        mRightStatement = rightStatement;
    }

}
