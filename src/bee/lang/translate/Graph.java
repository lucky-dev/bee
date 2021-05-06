package bee.lang.translate;

import java.util.LinkedList;

public class Graph {

    private LinkedList<Node> mAllNodes;

    public Graph() {
        mAllNodes = new LinkedList<>();
    }

    public LinkedList<Node> nodes() {
        return mAllNodes;
    }

    public Node newNode() {
        Node node = new Node(this);
        mAllNodes.add(node);
        return node;
    }

    public void addEdge(Node from, Node to) {
        from.addSucc(to);
        to.addPred(from);
    }

    public void rmEdge(Node from, Node to) {
        from.removeSucc(to);
        to.removePred(from);
    }

}
