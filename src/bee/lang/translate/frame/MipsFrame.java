package bee.lang.translate.frame;

import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.*;

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
        mFormalArguments = new LinkedList<>();
    }

    public Frame newFrame(Label name, LinkedList<Boolean> args) {
        mName = name;

        MipsFrame mipsFrame = new MipsFrame();

        Access access;
        int i = 0;
        Iterator<Boolean> iterator = args.iterator();

        while (iterator.hasNext()) {
            boolean isInFrame = iterator.next();

            if (i > 3) {
                access = new InFrame(mOffsetArgs);
                mOffsetArgs += 4;
            } else {
                access = (isInFrame ? new InFrame(getWordSize() * i) : new InReg(mRegArgs[i]));
            }

            mFormalArguments.add(access);

            i++;
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

    public Temp getRV() {
        return mRV;
    }

    @Override
    public Temp getFirstArg() {
        return mA0;
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
