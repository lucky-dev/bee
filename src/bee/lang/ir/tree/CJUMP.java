package bee.lang.ir.tree;

import bee.lang.ir.Label;

import java.util.LinkedList;

public class CJUMP extends IRStatement {

    private TypeRelOp mTypeRelOp;
    private IRExpression mLeftExpression;
    private IRExpression mRightExpression;
    private Label mLblTrue;
    private Label mLblFalse;

    public CJUMP(TypeRelOp typeRelOp, IRExpression leftExpression, IRExpression rightExpression, Label lblTrue, Label lblFalse) {
        mTypeRelOp = typeRelOp;
        mLeftExpression = leftExpression;
        mRightExpression = rightExpression;
        mLblTrue = lblTrue;
        mLblFalse = lblFalse;
    }

    public TypeRelOp getTypeRelOp() {
        return mTypeRelOp;
    }

    public IRExpression getLeftExpression() {
        return mLeftExpression;
    }

    public IRExpression getRightExpression() {
        return mRightExpression;
    }

    public Label getLblTrue() {
        return mLblTrue;
    }

    public Label getLblFalse() {
        return mLblFalse;
    }

    @Override
    public String toString() {
        return "CJUMP(" + mTypeRelOp + ", " + mLeftExpression + ", " + mRightExpression + ", " + mLblTrue + ", " + mLblFalse + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        LinkedList<IRExpression> kids = new LinkedList<>();
        kids.add(mLeftExpression);
        kids.add(mRightExpression);
        return kids;
    }

    @Override
    public IRStatement build(LinkedList<IRExpression> kids) {
        return new CJUMP(mTypeRelOp, kids.getFirst(), kids.getLast(), mLblTrue, mLblFalse);
    }

}
