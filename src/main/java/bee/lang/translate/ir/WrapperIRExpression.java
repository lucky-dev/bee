package bee.lang.translate.ir;

import bee.lang.ir.Label;
import bee.lang.ir.tree.IRExpression;
import bee.lang.ir.tree.IRStatement;

public abstract class WrapperIRExpression {

    public abstract IRExpression unEx();
    public abstract IRStatement unNx();
    public abstract IRStatement unCx(Label lblTrue, Label lblFalse);

}
