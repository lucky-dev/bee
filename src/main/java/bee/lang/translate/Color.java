package bee.lang.translate;

import bee.lang.assembly.TempMap;
import bee.lang.exceptions.SelectColorException;
import bee.lang.ir.Temp;
import bee.lang.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

// This class is used to set colors for temporary variables.
public class Color implements TempMap {

    private HashMap<Node, Temp> mColorsForTemps;
    private InterferenceGraph mInterferenceGraph;
    private TempMap mInitTempMap;

    public Color(InterferenceGraph interferenceGraph, TempMap initial, LinkedList<Temp> registers, int countRegisters) throws SelectColorException {
        mInterferenceGraph = interferenceGraph;

        mInitTempMap = initial;

        mColorsForTemps = new HashMap<>();

        LinkedList<Node> availableNodes = new LinkedList<>();

        HashMap<Node, LinkedList<Pair<Node, Node>>> savedEdges = new HashMap<>();

        for (Node node : mInterferenceGraph.nodes()) {
            if (initial.tempMap(mInterferenceGraph.getTemp(node)) == null) {
                if (node.degree() < countRegisters) {
                    availableNodes.add(node);
                }

                savedEdges.put(node, new LinkedList<>());
            } else {
                mColorsForTemps.put(node, mInterferenceGraph.getTemp(node));
            }
        }

        // Simplify
        LinkedList<Node> stack = new LinkedList<>();

        while (!availableNodes.isEmpty()) {
            Node node = availableNodes.pollFirst();

            for (Node adjNode : node.adj()) {
                if (node.goesTo(adjNode)) {
                    mInterferenceGraph.rmEdge(node, adjNode);
                    savedEdges.get(node).add(new Pair<>(node, adjNode));
                } else {
                    mInterferenceGraph.rmEdge(adjNode, node);
                    savedEdges.get(node).add(new Pair<>(adjNode, node));
                }

                if ((initial.tempMap(mInterferenceGraph.getTemp(adjNode)) == null) && (adjNode.degree() < countRegisters)) {
                    availableNodes.add(adjNode);
                }
            }

            mInterferenceGraph.removeNode(node);

            stack.push(node);
        }

        // Color
        while (!stack.isEmpty()) {
            Node node = stack.pop();

            mInterferenceGraph.addNode(node);

            for (Pair<Node, Node> edge : savedEdges.get(node)) {
                mInterferenceGraph.addEdge(edge.getFirst(), edge.getSecond());
            }

            HashSet<Temp> colorsOfNeighbors = new HashSet<>();

            for (Node adjNode : node.adj()) {
                colorsOfNeighbors.add(mColorsForTemps.get(adjNode));
            }

            for (Temp temp : registers) {
                if (!colorsOfNeighbors.contains(temp)) {
                    mColorsForTemps.put(node, temp);
                    break;
                }
            }
        }

        for (Node node : mInterferenceGraph.nodes()) {
            if (mInitTempMap.tempMap(mColorsForTemps.get(node)) == null) {
                // Need to spill temporary variable
                throw new SelectColorException(mInterferenceGraph.getTemp(node));
            }
        }
    }

    @Override
    public String tempMap(Temp temp) {
        String tempName = mInitTempMap.tempMap(mColorsForTemps.get(mInterferenceGraph.getNode(temp)));
        if (tempName == null) {
            // If some temporary variable is pre-colored and does not interfere with other temporaries then find it in the frame.
            return mInitTempMap.tempMap(temp);
        }

        return tempName;
    }

    public LinkedList<Temp> spills() {
        return new LinkedList<>();
    }

}
