package bee.lang.ir.tree;

import java.util.LinkedList;

public abstract class IRExpression {

    abstract public LinkedList<IRExpression> kids();
    abstract public IRExpression build(LinkedList<IRExpression> kids);

}
