package bee.lang.ir.tree;

public class MOVE extends IRStatement {

    private IRExpression mDst;
    private IRExpression mSrc;

    public MOVE(IRExpression dst, IRExpression src) {
        mDst = dst;
        mSrc = src;
    }

    public IRExpression getDst() {
        return mDst;
    }

    public IRExpression getSrc() {
        return mSrc;
    }

}
