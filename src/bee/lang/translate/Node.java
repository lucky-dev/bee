package bee.lang.translate;

import java.util.HashSet;
import java.util.LinkedList;

public class Node {

    private Graph mGraph;
    private HashSet<Node> mSuccList;
    private HashSet<Node> mPredList;

    public Node(Graph graph) {
        mGraph = graph;
        mSuccList = new HashSet<>();
        mPredList = new HashSet<>();
    }

    public void addSucc(Node node) {
        mSuccList.add(node);
    }

    public void removeSucc(Node node) {
        mSuccList.remove(node);
    }

    public LinkedList<Node> succ() {
        return new LinkedList<>(mSuccList);
    }

    public void addPred(Node node) {
        mPredList.add(node);
    }

    public void removePred(Node node) {
        mPredList.remove(node);
    }

    public LinkedList<Node> pred() {
        return new LinkedList<>(mPredList);
    }

    public LinkedList<Node> adj() {
        LinkedList<Node> list = new LinkedList<>();
        list.addAll(succ());
        list.addAll(pred());
        return list;
    }

    public int outDegree() {
        return succ().size();
    }

    public int inDegree() {
        return pred().size();
    }

    public int degree() {
        return inDegree() + outDegree();
    }

    public boolean goesTo(Node node) {
        return mSuccList.contains(node);
    }

    public boolean comesFrom(Node node) {
        return mPredList.contains(node);
    }

    public boolean adj(Node node) {
        return mSuccList.contains(node) || mPredList.contains(node);
    }

}
