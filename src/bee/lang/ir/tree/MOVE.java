package bee.lang.ir.tree;

import java.util.LinkedList;

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

    @Override
    public String toString() {
        return "MOVE(" + mDst + ", " + mSrc + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        LinkedList<IRExpression> kids = new LinkedList<>();

        if (mDst instanceof MEM) {
            kids.add(mDst);
        }

        kids.add(mSrc);

        return kids;
    }

    @Override
    public IRStatement build(LinkedList<IRExpression> kids) {
        IRExpression dst = mDst;

        if (mDst instanceof MEM) {
            dst = kids.getFirst();
        }

        return new MOVE(dst, kids.getLast());
    }

}
