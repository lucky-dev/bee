package bee.lang.translate.ir;

import bee.lang.ir.Label;
import bee.lang.ir.tree.CJUMP;
import bee.lang.ir.tree.IRExpression;
import bee.lang.ir.tree.IRStatement;
import bee.lang.ir.tree.TypeRelOp;

public class RelCx extends Cx {

    private TypeRelOp mTypeRelOp;
    private IRExpression mLeftExpression;
    private IRExpression mRightExpression;

    public RelCx(TypeRelOp typeRelOp, IRExpression leftExpression, IRExpression rightExpression) {
        mTypeRelOp = typeRelOp;
        mLeftExpression = leftExpression;
        mRightExpression = rightExpression;
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

    @Override
    public IRStatement unNx() {
        return null;
    }

    @Override
    public IRStatement unCx(Label lblTrue, Label lblFalse) {
        return new CJUMP(mTypeRelOp, mLeftExpression, mRightExpression, lblTrue, lblFalse);
    }

}
