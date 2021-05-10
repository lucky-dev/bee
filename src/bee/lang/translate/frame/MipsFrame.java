package bee.lang.translate.frame;

import bee.lang.assembly.AsmInstruction;
import bee.lang.assembly.AsmOPER;
import bee.lang.assembly.MipsCodegen;
import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.*;
import bee.lang.translate.ProcedureFragment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

// This class is used for working with stack frame (activation record) for a particular platform.
public class MipsFrame extends Frame {

    // All registers
    private static Temp sZero = new Temp(); // $0 : $zero
    private static Temp sAT = new Temp();   // $1 : $at
    // Results from functions
    private static Temp sV0 = new Temp();   // $2 : $v0
    private static Temp sV1 = new Temp();   // $3 : $v1
    // Arguments of functions
    private static Temp sA0 = new Temp();   // $4 : $a0
    private static Temp sA1 = new Temp();   // $5 : $a1
    private static Temp sA2 = new Temp();   // $6 : $a2
    private static Temp sA3 = new Temp();   // $7 : $a3
    // Callee-saved temps
    private static Temp sT0 = new Temp();   // $8 : $t0
    private static Temp sT1 = new Temp();   // $9 : $t1
    private static Temp sT2 = new Temp();   // $10 : $t2
    private static Temp sT3 = new Temp();   // $11 : $t3
    private static Temp sT4 = new Temp();   // $12 : $t4
    private static Temp sT5 = new Temp();   // $13 : $t5
    private static Temp sT6 = new Temp();   // $14 : $t6
    private static Temp sT7 = new Temp();   // $15 : $t7
    // Caller-saved
    private static Temp sS0 = new Temp();   // $16 : $s0
    private static Temp sS1 = new Temp();   // $17 : $s1
    private static Temp sS2 = new Temp();   // $18 : $s2
    private static Temp sS3 = new Temp();   // $19 : $s3
    private static Temp sS4 = new Temp();   // $20 : $s4
    private static Temp sS5 = new Temp();   // $21 : $s5
    private static Temp sS6 = new Temp();   // $22 : $s6
    private static Temp sS7 = new Temp();   // $23 : $s7
    // Callee-saved temps
    private static Temp sT8 = new Temp();   // $24 : $t8
    private static Temp sT9 = new Temp();   // $25 : $t9
    // Reserved for OS
    private static Temp sK0 = new Temp();   // $26 : $k0
    private static Temp sK1 = new Temp();   // $27 : $k1
    // Global pointer
    private static Temp sGP = new Temp();   // $28 : $gp
    // Stack pointer
    private static Temp sSP = new Temp();   // $29 : $sp
    // Frame pointer
    private static Temp sFP = new Temp();   // $30 : $fp
    // Return address
    private static Temp sRA = new Temp();   // $31 : $ra

    private static HashMap<Temp, String> sNamesOfRegs = new HashMap<>();

    static {
        sNamesOfRegs.put(sZero, "$zero");
        sNamesOfRegs.put(sAT, "$at");
        sNamesOfRegs.put(sV0, "$v0");
        sNamesOfRegs.put(sV1, "$v1");
        sNamesOfRegs.put(sA0, "$a0");
        sNamesOfRegs.put(sA1, "$a1");
        sNamesOfRegs.put(sA2, "$a2");
        sNamesOfRegs.put(sA3, "$a3");
        sNamesOfRegs.put(sT0, "$t0");
        sNamesOfRegs.put(sT1, "$t1");
        sNamesOfRegs.put(sT2, "$t2");
        sNamesOfRegs.put(sT3, "$t3");
        sNamesOfRegs.put(sT4, "$t4");
        sNamesOfRegs.put(sT5, "$t5");
        sNamesOfRegs.put(sT6, "$t6");
        sNamesOfRegs.put(sT7, "$t7");
        sNamesOfRegs.put(sS0, "$s0");
        sNamesOfRegs.put(sS1, "$s1");
        sNamesOfRegs.put(sS2, "$s2");
        sNamesOfRegs.put(sS3, "$s3");
        sNamesOfRegs.put(sS4, "$s4");
        sNamesOfRegs.put(sS5, "$s5");
        sNamesOfRegs.put(sS6, "$s6");
        sNamesOfRegs.put(sS7, "$s7");
        sNamesOfRegs.put(sT8, "$t8");
        sNamesOfRegs.put(sT9, "$t9");
        sNamesOfRegs.put(sK0, "$k0");
        sNamesOfRegs.put(sK1, "$k1");
        sNamesOfRegs.put(sGP, "$gp");
        sNamesOfRegs.put(sSP, "$sp");
        sNamesOfRegs.put(sFP, "$fp");
        sNamesOfRegs.put(sRA, "$ra");
    }

    private LinkedList<Access> mFormalArguments;
    private LinkedList<Access> mFormalArgumentsInFunction;
    private Label mName;
    private Temp[] mRegArgs;
    private LinkedList<Temp> mSpecialRegs;
    private LinkedList<Temp> mArgRegs;
    private LinkedList<Temp> mCalleeSavesRegs;
    private LinkedList<Temp> mMoreCalleeSavesRegs;
    private LinkedList<Temp> mCallerSavesRegs;
    private LinkedList<Temp> mReturnSink;
    private LinkedList<Temp> mReturnValueRegs;
    private int mCountLocalVarsInFrame;
    private IRStatement mMoveStatements;
    private LinkedList<Temp> mAllRegisters;

    public MipsFrame() {
        mSpecialRegs = new LinkedList<>();
        mSpecialRegs.add(sZero);
        mSpecialRegs.add(sAT);
        mSpecialRegs.add(sK0);
        mSpecialRegs.add(sK1);
        mSpecialRegs.add(sGP);
        mSpecialRegs.add(sSP);
        mSpecialRegs.add(sFP);
        mSpecialRegs.add(sRA);

        mArgRegs = new LinkedList<>();
        mArgRegs.add(sA0);
        mArgRegs.add(sA1);
        mArgRegs.add(sA2);
        mArgRegs.add(sA3);

        mReturnValueRegs = new LinkedList<>();
        mReturnValueRegs.add(sV0);
        mReturnValueRegs.add(sV1);

        mCalleeSavesRegs = new LinkedList<>();
        mCalleeSavesRegs.add(sT0);
        mCalleeSavesRegs.add(sT1);
        mCalleeSavesRegs.add(sT2);
        mCalleeSavesRegs.add(sT3);
        mCalleeSavesRegs.add(sT4);
        mCalleeSavesRegs.add(sT5);
        mCalleeSavesRegs.add(sT6);
        mCalleeSavesRegs.add(sT7);

        mMoreCalleeSavesRegs = new LinkedList<>();
        mMoreCalleeSavesRegs.add(sT8);
        mMoreCalleeSavesRegs.add(sT9);

        mCallerSavesRegs = new LinkedList<>();
        mCallerSavesRegs.add(sS0);
        mCallerSavesRegs.add(sS1);
        mCallerSavesRegs.add(sS2);
        mCallerSavesRegs.add(sS3);
        mCallerSavesRegs.add(sS4);
        mCallerSavesRegs.add(sS5);
        mCallerSavesRegs.add(sS6);
        mCallerSavesRegs.add(sS7);

        mAllRegisters = new LinkedList<>();
//        mAllRegisters.addAll(mSpecialRegs);
//        mAllRegisters.addAll(mArgRegs);
//        mAllRegisters.addAll(mReturnValueRegs);
//        mAllRegisters.addAll(mMoreCalleeSavesRegs);
        mAllRegisters.addAll(mCallerSavesRegs);
        mAllRegisters.addAll(mCalleeSavesRegs);

        mReturnSink = new LinkedList<>();
        mReturnSink.addAll(mSpecialRegs);
        mReturnSink.addAll(mCalleeSavesRegs);

        mRegArgs = new Temp[] { sA0, sA1, sA2, sA3 };
        mFormalArguments = new LinkedList<>();
        mCountLocalVarsInFrame = 0;
        mFormalArgumentsInFunction = new LinkedList<>();
    }

    @Override
    public Frame newFrame(Label name, LinkedList<Boolean> args) {
        mName = name;

        int i = 0;
        Iterator<Boolean> iterator = args.iterator();

        while (iterator.hasNext()) {
            boolean isInFrame = iterator.next();

            Access access;
            if (i > 3) {
                access = new InFrame(i * getWordSize());
            } else {
                access = (isInFrame ? new InFrame(i * getWordSize()) : new InReg(mRegArgs[i]));
            }

            mFormalArguments.add(access);
            mFormalArgumentsInFunction.add(new InFrame(i * getWordSize()));

            i++;
        }

        i = 0;
        for (Access access : mFormalArguments) {
            if (access instanceof InReg) {
                InFrame dst = new InFrame(i * getWordSize());
                IRStatement move = new MOVE(dst.exp(new TEMP(getFP())), access.exp(new TEMP(getFP())));
                if (mMoveStatements == null) {
                    mMoveStatements = move;
                } else {
                    mMoveStatements = new SEQ(mMoveStatements, move);
                }
            }
            i++;
        }

        return this;
    }

    @Override
    public Access allocLocal(boolean inFrame) {
        Access access;

        if (inFrame) {
            mCountLocalVarsInFrame++;
            access = new InFrame(-(mCountLocalVarsInFrame * getWordSize()));
        } else {
            access = new InReg(new Temp());
        }

        return access;
    }

    @Override
    public LinkedList<Temp> getSpecialRegs() {
        return mSpecialRegs;
    }

    @Override
    public LinkedList<Temp> getArgRegs() {
        return mArgRegs;
    }

    @Override
    public LinkedList<Temp> getReturnValueRegs() {
        return mReturnValueRegs;
    }

    @Override
    public LinkedList<Temp> getCalleeSavesRegs() {
        return mCalleeSavesRegs;
    }

    @Override
    public LinkedList<Temp> getCallerSavesRegs() {
        return mCallerSavesRegs;
    }

    @Override
    public int getWordSize() {
        return 4;
    }

    @Override
    public String getProcedureName() {
        return mName.getName();
    }

    @Override
    public Temp getFP() {
        return sFP;
    }

    @Override
    public Temp getRV() {
        return sV0;
    }

    @Override
    public Temp getRA() {
        return sRA;
    }

    @Override
    public LinkedList<Access> getFormalArguments() {
        return mFormalArguments;
    }

    @Override
    public LinkedList<Access> getFormalArgumentsInFunction() {
        return mFormalArgumentsInFunction;
    }

    @Override
    public IRExpression externalCall(String functionName, LinkedList<IRExpression> args) {
        return new CALL(new NAME(Label.newLabel(functionName)), args);
    }

    @Override
    public IRStatement procEntryExit1(IRStatement statement) {
        // Generate MOVE instructions to save callee-saved registers in the stack frame.
        LinkedList<Access> listOfFrameSpacesForLocals = new LinkedList<>();
        LinkedList<Temp> listOfTempsForCalleeSavedRegs = new LinkedList<>();

        Iterator<Temp> calleeSavedRegsIterator = mCalleeSavesRegs.iterator();
        Temp calleeSavedReg = calleeSavedRegsIterator.next();
        listOfFrameSpacesForLocals.add(allocLocal(true));
        IRStatement calleeSavedRegsSaveInstructions = new MOVE(listOfFrameSpacesForLocals.getLast().exp(new TEMP(getFP())), new TEMP(calleeSavedReg));
        while (calleeSavedRegsIterator.hasNext()) {
            calleeSavedReg = calleeSavedRegsIterator.next();
            listOfFrameSpacesForLocals.add(allocLocal(true));
            calleeSavedRegsSaveInstructions = new SEQ(new MOVE(listOfFrameSpacesForLocals.getLast().exp(new TEMP(getFP())), new TEMP(calleeSavedReg)), calleeSavedRegsSaveInstructions);
        }

        // Generate MOVE instructions to restore callee-saved registers from the stack frame.
        calleeSavedRegsIterator = mCalleeSavesRegs.iterator();
        Iterator<Access> listOfFrameSpacesForLocalsIterator = listOfFrameSpacesForLocals.iterator();
        calleeSavedReg = calleeSavedRegsIterator.next();
        IRStatement calleeSavedRegsRestoreInstructions = new MOVE(new TEMP(calleeSavedReg), listOfFrameSpacesForLocalsIterator.next().exp(new TEMP(getFP())));
        while (calleeSavedRegsIterator.hasNext()) {
            calleeSavedReg = calleeSavedRegsIterator.next();
            calleeSavedRegsRestoreInstructions = new SEQ(new MOVE(new TEMP(calleeSavedReg), listOfFrameSpacesForLocalsIterator.next().exp(new TEMP(getFP()))), calleeSavedRegsRestoreInstructions);
        }

        // Add instructions 'MOVE' to move all incoming arguments in the stack frame.
        IRStatement body = (mMoveStatements == null) ? statement : new SEQ(mMoveStatements, statement);

        // Add instructions 'MOVE' to save and restore all callee-saved registers.
        body = new SEQ(calleeSavedRegsSaveInstructions, new SEQ(body, calleeSavedRegsRestoreInstructions));

        return body;
    }

    @Override
    public LinkedList<AsmInstruction> procEntryExit2(LinkedList<AsmInstruction> body) {
        body.add(new AsmOPER("", new LinkedList<>(), mReturnSink));
        return body;
    }

    @Override
    public ProcedureFragment procEntryExit3(LinkedList<AsmInstruction> body) {
        return null;
    }

    @Override
    public LinkedList<AsmInstruction> codegen(IRStatement statement) {
        MipsCodegen mipsCodegen = new MipsCodegen(this);
        return mipsCodegen.codegen(statement);
    }

    @Override
    public String tempMap(Temp temp) {
        return sNamesOfRegs.get(temp);
    }

    @Override
    public LinkedList<Temp> registers() {
        return mAllRegisters;
    }

    @Override
    public int getCountRegisters() {
        return mSpecialRegs.size() +
                mArgRegs.size() +
                mReturnValueRegs.size() +
                mMoreCalleeSavesRegs.size() +
                mCalleeSavesRegs.size() +
                mCallerSavesRegs.size();
    }

}
