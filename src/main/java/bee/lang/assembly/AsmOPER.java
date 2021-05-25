package bee.lang.assembly;

import bee.lang.ir.Label;
import bee.lang.ir.Temp;

import java.util.LinkedList;

public class AsmOPER extends AsmInstruction {

    private LinkedList<Temp> mDst;
    private LinkedList<Temp> mSrc;
    private LinkedList<Label> mJump;

    public AsmOPER(String instruction, LinkedList<Temp> dst, LinkedList<Temp> src, LinkedList<Label> jump) {
        mInstruction = instruction;
        mDst = dst;
        mSrc = src;
        mJump = jump;
    }

    public AsmOPER(String asmInstruction, LinkedList<Temp> dst, LinkedList<Temp> src) {
        this(asmInstruction, dst, src, new LinkedList<>());
    }

    public AsmOPER(String asmInstruction) {
        this(asmInstruction, new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
    }

    @Override
    public LinkedList<Temp> getUse() {
        return mSrc;
    }

    @Override
    public LinkedList<Temp> getDef() {
        return mDst;
    }

    @Override
    public LinkedList<Label> getJumps() {
        return mJump;
    }

}
