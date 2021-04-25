package bee.lang.translate;

import bee.lang.ir.Temp;

import java.util.LinkedList;

public abstract class FlowGraph extends Graph {

    public abstract LinkedList<Temp> def(Node node);
    public abstract LinkedList<Temp> use(Node node);
    public abstract boolean isMove(Node node);

}
