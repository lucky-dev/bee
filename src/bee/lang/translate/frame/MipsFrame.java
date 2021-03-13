package bee.lang.translate.frame;

import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

// This class is used for working with stack frame (activation record) for a particular platform.
public class MipsFrame extends Frame {

    private int mOffsetLocals;
    private int mOffsetArgs;
    private Temp mFP;
    private Temp mRV;
    private Temp mA0;
    private Temp mA1;
    private Temp mA2;
    private Temp mA3;
    private Temp[] mRegArgs;

    public MipsFrame() {
        mOffsetArgs = getWordSize() * 4;
        mOffsetLocals = 0;
        mFP = new Temp();
        mRV = new Temp();
        mA0 = new Temp();
        mA1 = new Temp();
        mA2 = new Temp();
        mA3 = new Temp();
        mRegArgs = new Temp[] { mA0, mA1, mA2, mA3 };
        mFormalArguments = new ArrayList<>();
        mLocalVariables = new ArrayList<>();
    }

    public Frame newFrame(Label name, LinkedList<Boolean> args) {
        MipsFrame mipsFrame = new MipsFrame();

        mipsFrame.mName = name;

        Access access;
        int i = 0;
        Iterator<Boolean> iterator = args.iterator();

        while (iterator.hasNext()) {
            boolean isInFrame = iterator.next();

            if (i > 3) {
                access = new InFrame(mipsFrame.mOffsetArgs);
                mipsFrame.mOffsetArgs += 4;
            } else {
                access = (isInFrame ? new InFrame(getWordSize() * i) : new InReg(mipsFrame.mRegArgs[i]));
            }

            mipsFrame.mFormalArguments.add(access);

            i++;
        }

        return mipsFrame;
    }

    public int allocLocal(boolean inFrame) {
        Access access;

        if (inFrame) {
            mOffsetLocals -= 4;
            access = new InFrame(mOffsetLocals);
        } else {
            access = new InReg(new Temp());
        }

        mLocalVariables.add(access);

        return mLocalVariables.size() - 1;
    }

    public int getWordSize() {
        return 4;
    }

    @Override
    public Temp getFP() {
        return mFP;
    }

    public Temp getRV() {
        return mRV;
    }

    @Override
    public Access getFormalArg(int index) {
        return mFormalArguments.get(index);
    }

    @Override
    public Access getLocalVar(int index) {
        return mLocalVariables.get(index);
    }

    @Override
    public IRExpression externalCall(String functionName, LinkedList<IRExpression> args) {
        return new CALL(new NAME(Label.newLabel(functionName)), args);
    }

    @Override
    public IRStatement procEntryExit1(IRStatement statement) {
        return statement;
    }

}
