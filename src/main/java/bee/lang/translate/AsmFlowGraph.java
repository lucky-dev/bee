package bee.lang.translate;

import bee.lang.assembly.AsmInstruction;
import bee.lang.assembly.AsmLABEL;
import bee.lang.assembly.AsmMOVE;
import bee.lang.assembly.TempMap;
import bee.lang.ir.Label;
import bee.lang.ir.Temp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

// This class is used to create a control-flow graph. This graph will be used for liveness analysis. Each node of the graph is an instruction. Each edge of the graph is path from one instruction to other instruction.
public class AsmFlowGraph extends FlowGraph {

    private HashMap<Node, AsmInstruction> mNodeInfo;
    private HashMap<AsmInstruction, Node> mReverseNodeInfo;
    private HashMap<Label, Node> mNodeLabels;
    private LinkedList<AsmInstruction> mAsmInstructions;

    public AsmFlowGraph(LinkedList<AsmInstruction> asmInstructions) {
        mAsmInstructions = asmInstructions;

        mNodeInfo = new HashMap<>();
        mReverseNodeInfo = new HashMap<>();
        mNodeLabels = new HashMap<>();

        // Create only nodes
        AsmLABEL prevAsmLabel = null;
        Iterator<AsmInstruction> iterator = asmInstructions.iterator();
        while (iterator.hasNext()) {
            AsmInstruction asmInstruction = iterator.next();

            if (asmInstruction instanceof AsmLABEL) {
                prevAsmLabel = (AsmLABEL) asmInstruction;
            } else {
                Node node = newNode();
                mNodeInfo.put(node, asmInstruction);
                mReverseNodeInfo.put(asmInstruction, node);

                if (prevAsmLabel != null) {
                    mNodeLabels.put(prevAsmLabel.getLabel(), node);
                    prevAsmLabel = null;
                }
            }
        }

        // Add edges
        iterator = asmInstructions.iterator();
        AsmInstruction prevAsmInstruction = null;
        while (iterator.hasNext()) {
            AsmInstruction asmInstruction = iterator.next();

            if (asmInstruction instanceof AsmLABEL) {
                continue;
            }

            if (prevAsmInstruction != null) {
                addEdge(mReverseNodeInfo.get(prevAsmInstruction), mReverseNodeInfo.get(asmInstruction));
            }

            LinkedList<Label> jumps = asmInstruction.getJumps();
            if (jumps != null) {
                Iterator<Label> jumpsIterator = jumps.iterator();
                while (jumpsIterator.hasNext()) {
                    Node node = mNodeLabels.get(jumpsIterator.next());
                    if (node != null) {
                        addEdge(mReverseNodeInfo.get(asmInstruction), node);
                    }
                }
            }

            prevAsmInstruction = asmInstruction;
        }
    }

    public AsmInstruction asmInstruction(Node node) {
        return mNodeInfo.get(node);
    }

    @Override
    public LinkedList<Temp> def(Node node) {
        return asmInstruction(node).getDef();
    }

    @Override
    public LinkedList<Temp> use(Node node) {
        return asmInstruction(node).getUse();
    }

    @Override
    public boolean isMove(Node node) {
        return asmInstruction(node) instanceof AsmMOVE;
    }

    public void print(TempMap tempMap) {
        StringBuilder sb = new StringBuilder();

        Iterator<AsmInstruction> iterator = mAsmInstructions.iterator();

        while (iterator.hasNext()) {
            AsmInstruction asmInstruction = iterator.next();

            if (asmInstruction instanceof AsmLABEL) {
                continue;
            }

            sb.append(asmInstruction.format(tempMap));
            sb.append(" -> ");

            LinkedList<Node> listNodes = mReverseNodeInfo.get(asmInstruction).succ();

            if (listNodes != null) {
                Iterator<Node> iteratorNode = listNodes.iterator();
                while (iteratorNode.hasNext()) {
                    Node node = iteratorNode.next();
                    sb.append(asmInstruction(node).format(tempMap));
                    if (listNodes.getLast() != node) {
                        sb.append(", ");
                    }
                }
            }

            sb.append("\n");
        }

        System.out.println(sb);
    }

}
