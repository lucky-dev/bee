package bee.lang.translate.frame;

import bee.lang.ir.Label;
import bee.lang.ir.Temp;

import java.util.LinkedList;

// This class is used for working with stack frame (activation record) for a particular platform.
public class MipsFrame extends Frame {

    private int mOffsetLocals;
    private int mOffsetArgs;
    private Temp mFP;
    private Temp mA0;
    private Temp mA1;
    private Temp mA2;
    private Temp mA3;
    private LinkedList<Access> mFormalArguments;
    private Temp[] mRegArgs;

    public MipsFrame() {
        mOffsetArgs = 0;
        mOffsetLocals = 0;
        mFP = new Temp();
        mA0 = new Temp();
        mA1 = new Temp();
        mA2 = new Temp();
        mA3 = new Temp();
        mRegArgs = new Temp[] { mA0, mA1, mA2, mA3 };
        mFormalArguments = new LinkedList<>();
    }

    public Frame newFrame(Label name, boolean... args) {
        MipsFrame mipsFrame = new MipsFrame();

        for (int i = 0; i < args.length; i++) {
            Access access;

            if (i > 3) {
                access = new InFrame(mOffsetArgs);
                mOffsetArgs += 4;
            } else {
                if (args[i]) {
                    access = new InFrame(mOffsetArgs);
                    mOffsetArgs += 4;
                } else {
                    access = new InReg(mRegArgs[i]);
                }
            }

            mFormalArguments.add(access);
        }

        return mipsFrame;
    }

    public Access allocLocal(boolean inFrame) {
        if (inFrame) {
            mOffsetLocals -= 4;
            return new InFrame(mOffsetLocals);
        } else {
            return new InReg(new Temp());
        }
    }

    public int getWordSize() {
        return 4;
    }

    @Override
    public Temp getFP() {
        return mFP;
    }

    @Override
    public Temp getFirstArg() {
        return mA0;
    }

}
