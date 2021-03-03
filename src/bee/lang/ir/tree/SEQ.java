package bee.lang.ir.tree;

public class SEQ extends IRStatement {

    private IRStatement mLeftStatement;
    private IRStatement mRightStatement;

    public SEQ(IRStatement leftStatement, IRStatement rightStatement) {
        mLeftStatement = leftStatement;
        mRightStatement = rightStatement;
    }

    public IRStatement getLeftStatement() {
        return mLeftStatement;
    }

    public IRStatement getRightStatement() {
        return mRightStatement;
    }

}
