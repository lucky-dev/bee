package bee.lang.translate;

import bee.lang.ir.Label;
import bee.lang.ir.tree.EXP;
import bee.lang.ir.tree.IRExpression;
import bee.lang.ir.tree.IRStatement;

public class Ex extends WrapperIRExpression {

    private IRExpression mExpression;

    public Ex(IRExpression expression) {
        mExpression = expression;
    }

    @Override
    public IRExpression unEx() {
        return mExpression;
    }

    @Override
    public IRStatement unNx() {
        return new EXP(mExpression);
    }

    @Override
    public IRStatement unCx(Label lblTrue, Label lblFalse) {
        return null;
    }

}
