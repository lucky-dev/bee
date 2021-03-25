package bee.lang.translate.frame;

import bee.lang.assembly.AsmInstruction;
import bee.lang.assembly.TempMap;
import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.IRExpression;
import bee.lang.ir.tree.IRStatement;

import java.util.ArrayList;
import java.util.LinkedList;

public abstract class Frame implements TempMap {

    protected ArrayList<Access> mFormalArguments;
    protected ArrayList<Access> mLocalVariables;
    protected Label mName;

    public abstract Frame newFrame(Label name, LinkedList<Boolean> args);
    public abstract int allocLocal(boolean isInFrame);
    public abstract String getProcedureName();
    public abstract int getWordSize();
    public abstract Temp getFP();
    public abstract Temp getRV();
    public abstract Temp getRA();
    public abstract Access getFormalArg(int index);
    public abstract Access getLocalVar(int index);
    public abstract IRExpression externalCall(String functionName, LinkedList<IRExpression> args);
    public abstract IRStatement procEntryExit1(IRStatement statement);
    public abstract LinkedList<AsmInstruction> procEntryExit2(LinkedList<AsmInstruction> body);
    public abstract LinkedList<AsmInstruction> procEntryExit3(LinkedList<AsmInstruction> body);
    public abstract LinkedList<AsmInstruction> codegen(IRStatement statement);
    public abstract LinkedList<Temp> getSpecialRegs();
    public abstract LinkedList<Temp> getArgRegs();
    public abstract LinkedList<Temp> getCalleeSavesRegs();
    public abstract LinkedList<Temp> getCallerSavesRegs();

}
