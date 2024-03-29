package bee.lang.ir.tree;

import java.util.LinkedList;

public class BINOP extends IRExpression {

    private TypeBinOp mTypeBinOp;
    private IRExpression mLeftExpression;
    private IRExpression mRightExpression;

    public BINOP(TypeBinOp typeBinOp, IRExpression leftExpression, IRExpression rightExpression) {
        mTypeBinOp = typeBinOp;
        mLeftExpression = leftExpression;
        mRightExpression = rightExpression;
    }

    public TypeBinOp getTypeBinOp() {
        return mTypeBinOp;
    }

    public IRExpression getLeftExpression() {
        return mLeftExpression;
    }

    public IRExpression getRightExpression() {
        return mRightExpression;
    }

    @Override
    public String toString() {
        return "BINOP(" + mTypeBinOp + ", " + mLeftExpression + ", " + mRightExpression + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        LinkedList<IRExpression> kids = new LinkedList<>();
        kids.add(mLeftExpression);
        kids.add(mRightExpression);
        return kids;
    }

    @Override
    public IRExpression build(LinkedList<IRExpression> kids) {
        return new BINOP(mTypeBinOp, kids.getFirst(), kids.getLast());
    }

}
