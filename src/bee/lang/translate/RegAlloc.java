package bee.lang.translate;

import bee.lang.assembly.AsmInstruction;
import bee.lang.assembly.TempMap;
import bee.lang.ir.Temp;
import bee.lang.translate.frame.Frame;

import java.util.LinkedList;

// This class is a wrapper for liveness analysis and distribution of registers.
public class RegAlloc implements TempMap {

    private Color mColor;

    public RegAlloc(Frame frame, LinkedList<AsmInstruction> asmInstructions) {
        AsmFlowGraph asmFlowGraph = new AsmFlowGraph(asmInstructions);

        Liveness liveness = new Liveness(asmFlowGraph);

        mColor = new Color(liveness, frame, frame.registers());
    }

    @Override
    public String tempMap(Temp temp) {
        return mColor.tempMap(temp);
    }

}
