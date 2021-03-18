package bee.lang.ir.tree;

import java.util.LinkedList;

public abstract class IRStatement {

    abstract public LinkedList<IRExpression> kids();
    abstract public IRStatement build(LinkedList<IRExpression> kids);

}
