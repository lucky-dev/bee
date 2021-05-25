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
        addNode(node);
        return node;
    }

    public void addNode(Node node) {
        if (!mAllNodes.contains(node)) {
            mAllNodes.add(node);
        }
    }

    public void removeNode(Node node) {
        mAllNodes.remove(node);
    }

    public void addEdge(Node from, Node to) {
        if ((!from.goesTo(to)) && (!to.comesFrom(from))) {
            from.addSucc(to);
            to.addPred(from);
        }
    }

    public void rmEdge(Node from, Node to) {
        if ((from.goesTo(to)) && (to.comesFrom(from))) {
            from.removeSucc(to);
            to.removePred(from);
        }
    }

}
