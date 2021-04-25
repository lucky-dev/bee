package bee.lang.translate;

import bee.lang.ir.Temp;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class Liveness extends InterferenceGraph {

    private FlowGraph mFlowGraph;
    private HashMap<Node, Temp> mTempNode;
    private HashMap<Temp, Node> mReverseTempNode;
    private LinkedList<Pair<Temp, Temp>> mListMoves;

    public Liveness(FlowGraph flowGraph) {
        mFlowGraph = flowGraph;

        mTempNode = new HashMap<>();
        mReverseTempNode = new HashMap<>();
        mListMoves = new LinkedList<>();

        // Create a table of live-in and live-out variables.
        HashMap<Node, HashSet<Temp>> def = new HashMap<>();
        HashMap<Node, HashSet<Temp>> use = new HashMap<>();

        for (Node node : flowGraph.nodes()) {
            LinkedList<Temp> defTemps = flowGraph.def(node);
            LinkedList<Temp> useTemps = flowGraph.use(node);
            def.put(node, new HashSet<>(defTemps));
            use.put(node, new HashSet<>(useTemps));
            if (mFlowGraph.isMove(node)) {
                mListMoves.add(new Pair<>(useTemps.getFirst(), defTemps.getFirst()));
            }
        }

        HashMap<Node, HashSet<Temp>> liveIn = new HashMap<>();
        HashMap<Node, HashSet<Temp>> liveOut = new HashMap<>();

        HashMap<Node, HashSet<Temp>> _liveIn = new HashMap<>();
        HashMap<Node, HashSet<Temp>> _liveOut = new HashMap<>();

        for (Node node : flowGraph.nodes()) {
            liveIn.put(node, new HashSet<>());
            liveOut.put(node, new HashSet<>());
            _liveIn.put(node, new HashSet<>());
            _liveOut.put(node, new HashSet<>());
        }

        do {
            for (Node node : flowGraph.nodes()) {
                _liveIn.get(node).addAll(liveIn.get(node));
                _liveOut.get(node).addAll(liveOut.get(node));
                liveIn.get(node).addAll(use.get(node));
                HashSet<Temp> newLiveOut = new HashSet<>(liveOut.get(node));
                newLiveOut.removeAll(def.get(node));
                liveIn.get(node).addAll(newLiveOut);
                for (Node s : node.succ()) {
                    liveOut.get(node).addAll(liveIn.get(s));
                }
            }
        } while (!((isEqualMaps(liveIn, _liveIn)) && (isEqualMaps(liveOut, _liveOut))));

        // Create an interference graph. Each node of the graph represents a variable. Each edge of the graph represents interfere variables which must be allocated in different registers.
        for (Node node : flowGraph.nodes()) {
            HashSet<Temp> defTemps = def.get(node);
            HashSet<Temp> liveOutTemps = liveOut.get(node);

            for (Temp defTemp : defTemps) {
                Node nodeDefTemp = mReverseTempNode.get(defTemp);
                if (nodeDefTemp == null) {
                    nodeDefTemp = newNode();
                    mTempNode.put(nodeDefTemp, defTemp);
                    mReverseTempNode.put(defTemp, nodeDefTemp);
                }

                for (Temp liveOutTemp : liveOutTemps) {
                    if (defTemp != liveOutTemp) {
                        if (flowGraph.isMove(node)) {
                            if (!use.get(node).contains(liveOutTemp)) {
                                Node nodeLiveOutTemp = mReverseTempNode.get(liveOutTemp);
                                if (nodeLiveOutTemp == null) {
                                    nodeLiveOutTemp = newNode();
                                    mTempNode.put(nodeLiveOutTemp, liveOutTemp);
                                    mReverseTempNode.put(liveOutTemp, nodeLiveOutTemp);
                                }

                                addEdge(nodeDefTemp, nodeLiveOutTemp);
                            }
                        } else {
                            Node nodeLiveOutTemp = mReverseTempNode.get(liveOutTemp);
                            if (nodeLiveOutTemp == null) {
                                nodeLiveOutTemp = newNode();
                                mTempNode.put(nodeLiveOutTemp, liveOutTemp);
                                mReverseTempNode.put(liveOutTemp, nodeLiveOutTemp);
                            }

                            addEdge(nodeDefTemp, nodeLiveOutTemp);
                        }
                    }
                }
            }
        }
    }

    private boolean isEqualMaps(HashMap<Node, HashSet<Temp>> map1, HashMap<Node, HashSet<Temp>> map2) {
        for (Node node : mFlowGraph.nodes()) {
            HashSet<Temp> set1 = map1.get(node);
            HashSet<Temp> set2 = map2.get(node);

            if ((set1.size() != set2.size()) || (!set1.containsAll(set2))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Node getNode(Temp temp) {
        return mReverseTempNode.get(temp);
    }

    @Override
    public Temp getTemp(Node node) {
        return mTempNode.get(node);
    }

    @Override
    public LinkedList<Pair<Temp, Temp>> moves() {
        return mListMoves;
    }

    public void print() {
        StringBuilder sb = new StringBuilder();

        Iterator<Node> iteratorNode = nodes().iterator();

        while (iteratorNode.hasNext()) {
            Node node = iteratorNode.next();

            Temp temp = mTempNode.get(node);
            sb.append(temp.toString());
            sb.append(" -> ");

            LinkedList<Node> listNodes = node.succ();

            Iterator<Node> iteratorListNodes =  listNodes.iterator();

            while (iteratorListNodes.hasNext()) {
                Node item = iteratorListNodes.next();
                Temp _temp = mTempNode.get(item);
                sb.append(_temp.toString());
                if (listNodes.getLast() != item) {
                    sb.append(", ");
                }
            }

            sb.append("\n");
        }

        System.out.println(sb);
    }

}
