package bee.lang.translate.frame;

import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.IRExpression;
import bee.lang.ir.tree.IRStatement;

import java.util.ArrayList;
import java.util.LinkedList;

public abstract class Frame {

    protected ArrayList<Access> mFormalArguments;
    protected ArrayList<Access> mLocalVariables;
    protected Label mName;

    public abstract Frame newFrame(Label name, LinkedList<Boolean> args);
    public abstract Access allocLocal(boolean isInFrame);
    public abstract int getWordSize();
    public abstract Temp getFP();
    public abstract Temp getRV();
    public abstract Access getFormalArg(int index);
    public abstract Access getLocalVar(int index);
    public abstract IRExpression externalCall(String functionName, LinkedList<IRExpression> args);
    public abstract IRStatement procEntryExit1(IRStatement statement);

}
