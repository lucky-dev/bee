package bee.lang.translate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Graph {

    private HashMap<Node, HashSet<Node>> mAdjList;
    private LinkedList<Node> mAllNodes;

    public Graph() {
        mAdjList = new HashMap<>();
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

        HashSet<Node> list = mAdjList.get(from);
        if (list == null) {
            list = new HashSet<>();
            mAdjList.put(from, list);
        }
        list.add(to);
    }

    public void rmEdge(Node from, Node to) {
        if (from != to) {
            mAdjList.get(from).remove(to);
        }
    }

}
