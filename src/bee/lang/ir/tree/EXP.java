package bee.lang.ir.tree;

public class EXP extends IRStatement {

    private IRExpression mExpression;

    public EXP(IRExpression expression) {
        mExpression = expression;
    }

    public IRExpression getExpression() {
        return mExpression;
    }

}
