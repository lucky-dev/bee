package bee.lang.translate.frame;

import bee.lang.ir.tree.*;

public class InFrame extends Access {

    private int mOffset;

    public InFrame(int offset) {
        mOffset = offset;
    }

    public int getOffset() {
        return mOffset;
    }

    @Override
    public IRExpression exp(IRExpression fp) {
        return new MEM(new BINOP(TypeBinOp.PLUS, fp, new CONST(mOffset)));
    }

}
