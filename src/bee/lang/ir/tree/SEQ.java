package bee.lang.ir.tree;

import java.util.LinkedList;

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

    @Override
    public String toString() {
        return "SEQ(" + mLeftStatement + ", " + mRightStatement + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        return null;
    }

    @Override
    public IRStatement build(LinkedList<IRExpression> kids) {
        return null;
    }

}
