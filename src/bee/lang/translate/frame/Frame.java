package bee.lang.translate.frame;

import bee.lang.assembly.AsmInstruction;
import bee.lang.assembly.TempMap;
import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.IRExpression;
import bee.lang.ir.tree.IRStatement;
import bee.lang.translate.ProcedureFragment;

import java.util.LinkedList;

public abstract class Frame implements TempMap {

    public abstract Frame newFrame(Label name, LinkedList<Boolean> args);
    public abstract Access allocLocal(boolean isInFrame);
    public abstract String getProcedureName();
    public abstract int getWordSize();
    public abstract Temp getFP();
    public abstract Temp getRV();
    public abstract Temp getRA();
    public abstract LinkedList<Access> getFormalArguments();
    public abstract LinkedList<Access> getFormalArgumentsInFunction();
    public abstract IRExpression externalCall(String functionName, LinkedList<IRExpression> args);
    public abstract IRStatement procEntryExit1(IRStatement statement);
    public abstract LinkedList<AsmInstruction> procEntryExit2(LinkedList<AsmInstruction> body);
    public abstract ProcedureFragment procEntryExit3(LinkedList<AsmInstruction> body);
    public abstract LinkedList<AsmInstruction> codegen(IRStatement statement);
    public abstract LinkedList<Temp> getSpecialRegs();
    public abstract LinkedList<Temp> getArgRegs();
    public abstract LinkedList<Temp> getReturnValueRegs();
    public abstract LinkedList<Temp> getCalleeSavesRegs();
    public abstract LinkedList<Temp> getCallerSavesRegs();
    public abstract LinkedList<Temp> registers();
    public abstract int getCountRegisters();

}
