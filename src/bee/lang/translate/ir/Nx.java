package bee.lang.translate.ir;

import bee.lang.ir.Label;
import bee.lang.ir.tree.IRExpression;
import bee.lang.ir.tree.IRStatement;

public class Nx extends WrapperIRExpression {

    private IRStatement mStatement;

    public Nx(IRStatement statement) {
        mStatement = statement;
    }

    @Override
    public IRExpression unEx() {
        return null;
    }

    @Override
    public IRStatement unNx() {
        return mStatement;
    }

    @Override
    public IRStatement unCx(Label lblTrue, Label lblFalse) {
        return null;
    }

}
