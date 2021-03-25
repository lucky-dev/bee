package bee.lang.assembly;

import bee.lang.ir.Label;
import bee.lang.ir.Temp;

import java.util.LinkedList;

public class AsmMOVE extends AsmInstruction {

    private Temp mDst;
    private Temp mSrc;

    public AsmMOVE(String instruction, Temp dst, Temp src) {
        mInstruction = instruction;
        mDst = dst;
        mSrc = src;
    }

    @Override
    public LinkedList<Temp> getUse() {
        LinkedList<Temp> list = new LinkedList<>();
        list.add(mSrc);
        return list;
    }

    @Override
    public LinkedList<Temp> getDef() {
        LinkedList<Temp> list = new LinkedList<>();
        list.add(mDst);
        return list;
    }

    @Override
    public LinkedList<Label> getJumps() {
        return null;
    }

}
