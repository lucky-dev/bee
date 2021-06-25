package bee.lang.translate;

import bee.lang.assembly.TempMap;
import bee.lang.ir.Temp;
import bee.lang.util.Pair;

import java.util.*;

// This class is used to compute live-in and live-out variables for each instructions. Live-in variables live before instruction and live-out variables live after instruction.
// After computing these variables this class creates an interference graph for register allocator. Each node of this graph is a temporary variable and each edge of this graph represents simultaneously life of two temporary variables.
public class Liveness extends InterferenceGraph {

    private FlowGraph mFlowGraph;
    private HashMap<Node, Temp> mTempNode;
    private HashMap<Temp, Node> mReverseTempNode;
    private LinkedList<Pair<Temp, Temp>> mListMoves;
    private HashMap<Node, HashSet<Temp>> mLiveIn;
    private HashMap<Node, HashSet<Temp>> mLiveOut;

    public Liveness(FlowGraph flowGraph) {
        mFlowGraph = flowGraph;

        mLiveIn = new HashMap<>();
        mLiveOut = new HashMap<>();

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

        HashMap<Node, HashSet<Temp>> _liveIn = new HashMap<>();
        HashMap<Node, HashSet<Temp>> _liveOut = new HashMap<>();

        for (Node node : flowGraph.nodes()) {
            mLiveIn.put(node, new HashSet<>());
            mLiveOut.put(node, new HashSet<>());
            _liveIn.put(node, new HashSet<>());
            _liveOut.put(node, new HashSet<>());
        }

        do {
            for (Node node : flowGraph.nodes()) {
                _liveIn.get(node).clear();
                _liveOut.get(node).clear();
                _liveIn.get(node).addAll(mLiveIn.get(node));
                _liveOut.get(node).addAll(mLiveOut.get(node));
                mLiveIn.get(node).addAll(use.get(node));
                HashSet<Temp> newLiveOut = new HashSet<>(mLiveOut.get(node));
                newLiveOut.removeAll(def.get(node));
                mLiveIn.get(node).addAll(newLiveOut);
                for (Node s : node.succ()) {
                    mLiveOut.get(node).addAll(mLiveIn.get(s));
                }
            }
        } while (!((isEqualMaps(mLiveIn, _liveIn)) && (isEqualMaps(mLiveOut, _liveOut))));

        // Create an interference graph. Each node of the graph represents a variable. Each edge of the graph represents interfere variables which must be allocated in different registers.
        for (Node node : flowGraph.nodes()) {
            HashSet<Temp> defTemps = def.get(node);
            HashSet<Temp> liveOutTemps = mLiveOut.get(node);

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

    private void printNodes(TempMap tempMap) {
        AsmFlowGraph asmFlowGraph = (AsmFlowGraph) mFlowGraph;

        for (Node node : asmFlowGraph.nodes()) {
            ArrayList<String> defList = new ArrayList<>();
            ArrayList<String> useList = new ArrayList<>();
            ArrayList<String> liveInList = new ArrayList<>();
            ArrayList<String> liveOutList = new ArrayList<>();

            for (Temp temp : asmFlowGraph.def(node)) {
                String tempName = tempMap.tempMap(temp);
                defList.add(tempName == null ? temp.toString() : tempName);
            }

            for (Temp temp : asmFlowGraph.use(node)) {
                String tempName = tempMap.tempMap(temp);
                useList.add(tempName == null ? temp.toString() : tempName);
            }

            for (Temp temp : mLiveIn.get(node)) {
                String tempName = tempMap.tempMap(temp);
                liveInList.add(tempName == null ? temp.toString() : tempName);
            }

            for (Temp temp : mLiveOut.get(node)) {
                String tempName = tempMap.tempMap(temp);
                liveOutList.add(tempName == null ? temp.toString() : tempName);
            }

            System.out.println(asmFlowGraph.asmInstruction(node).format(tempMap) + " -> def : " + defList + ", use: " + useList + ", live-in: " + liveInList + ", live-out: " + liveOutList);
        }
    }

    public void print(TempMap tempMap) {
        printNodes(tempMap);

        StringBuilder sb = new StringBuilder();

        for (Node node : nodes()) {
            Temp temp = mTempNode.get(node);
            String tempName = tempMap.tempMap(temp);
            sb.append(tempName == null ? temp.toString() : tempName);
            sb.append(" -> ");

            LinkedList<Node> listOfAdjNodes = node.adj();

            sb.append("(" + listOfAdjNodes.size() + ") ");

            for (Node adjNode : listOfAdjNodes) {
                Temp _temp = mTempNode.get(adjNode);
                tempName = tempMap.tempMap(_temp);
                sb.append(tempName == null ? _temp.toString() : tempName);
                if (listOfAdjNodes.getLast() != adjNode) {
                    sb.append(", ");
                }
            }

            sb.append("\n");
        }

        System.out.println(sb);
    }

}
