package bee.lang.translate;

import java.util.HashSet;
import java.util.LinkedList;

public class Node {

    private Graph mGraph;
    private HashSet<Node> mSuccList;
    private HashSet<Node> mPredList;
    private HashSet<Node> mTotalAdjNodes;

    public Node(Graph graph) {
        mGraph = graph;
        mSuccList = new HashSet<>();
        mPredList = new HashSet<>();
        mTotalAdjNodes = new HashSet<>();
    }

    public void addSucc(Node node) {
        if (mGraph == node.mGraph) {
            mSuccList.add(node);
            mTotalAdjNodes.add(node);
        }
    }

    public void removeSucc(Node node) {
        if (mGraph == node.mGraph) {
            mSuccList.remove(node);
            mTotalAdjNodes.remove(node);
        }
    }

    public LinkedList<Node> succ() {
        return new LinkedList<>(mSuccList);
    }

    public void addPred(Node node) {
        if (mGraph == node.mGraph) {
            mPredList.add(node);
            mTotalAdjNodes.add(node);
        }
    }

    public void removePred(Node node) {
        if (mGraph == node.mGraph) {
            mPredList.remove(node);
            mTotalAdjNodes.remove(node);
        }
    }

    public LinkedList<Node> pred() {
        return new LinkedList<>(mPredList);
    }

    public LinkedList<Node> adj() {
        return new LinkedList<>(mTotalAdjNodes);
    }

    public int outDegree() {
        return succ().size();
    }

    public int inDegree() {
        return pred().size();
    }

    public int degree() {
        return mTotalAdjNodes.size();
    }

    public boolean goesTo(Node node) {
        return ((mGraph == node.mGraph) && (mSuccList.contains(node)));
    }

    public boolean comesFrom(Node node) {
        return ((mGraph == node.mGraph) && (mPredList.contains(node)));
    }

    public boolean adj(Node node) {
        return ((mGraph == node.mGraph) && (mTotalAdjNodes.contains(node)));
    }

}
