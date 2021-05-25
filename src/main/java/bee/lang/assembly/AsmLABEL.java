package bee.lang.assembly;

import bee.lang.ir.Label;
import bee.lang.ir.Temp;

import java.util.LinkedList;

public class AsmLABEL extends AsmInstruction {

    private Label mLabel;

    public AsmLABEL(String instruction, Label label) {
        mInstruction = instruction;
        mLabel = label;
    }

    @Override
    public LinkedList<Temp> getUse() {
        return new LinkedList<>();
    }

    @Override
    public LinkedList<Temp> getDef() {
        return new LinkedList<>();
    }

    @Override
    public LinkedList<Label> getJumps() {
        return new LinkedList<>();
    }

    public Label getLabel() {
        return mLabel;
    }

}
