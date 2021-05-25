package bee.lang.translate;

import bee.lang.ir.Temp;
import javafx.util.Pair;

import java.util.LinkedList;

public abstract class InterferenceGraph extends Graph {

    abstract public Node getNode(Temp temp);

    abstract public Temp getTemp(Node node);

    abstract public LinkedList<Pair<Temp, Temp>> moves();

    public int spillCost(Node node) {
        return 1;
    }

}
