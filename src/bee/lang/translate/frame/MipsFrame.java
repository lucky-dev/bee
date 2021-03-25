package bee.lang.translate.frame;

import bee.lang.assembly.AsmInstruction;
import bee.lang.assembly.AsmOPER;
import bee.lang.assembly.MipsCodegen;
import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

// This class is used for working with stack frame (activation record) for a particular platform.
public class MipsFrame extends Frame {

    // All registers
    private Temp mZero; // $0 : $zero
    private Temp mAT;   // $1 : $at
    // Results from functions
    private Temp mV0;   // $2 : $v0
    private Temp mV1;   // $3 : $v1
    // Arguments of functions
    private Temp mA0;   // $4 : $a0
    private Temp mA1;   // $5 : $a1
    private Temp mA2;   // $6 : $a2
    private Temp mA3;   // $7 : $a3
    // Callee-saved temps
    private Temp mT0;   // $8 : $t0
    private Temp mT1;   // $9 : $t1
    private Temp mT2;   // $10 : $t2
    private Temp mT3;   // $11 : $t3
    private Temp mT4;   // $12 : $t4
    private Temp mT5;   // $13 : $t5
    private Temp mT6;   // $14 : $t6
    private Temp mT7;   // $15 : $t7
    // Caller-saved
    private Temp mS0;   // $16 : $s0
    private Temp mS1;   // $17 : $s1
    private Temp mS2;   // $18 : $s2
    private Temp mS3;   // $19 : $s3
    private Temp mS4;   // $20 : $s4
    private Temp mS5;   // $21 : $s5
    private Temp mS6;   // $22 : $s6
    private Temp mS7;   // $23 : $s7
    // Callee-saved temps
    private Temp mT8;   // $24 : $t8
    private Temp mT9;   // $25 : $t9
    // Reserved for OS
    private Temp mK0;   // $26 : $k0
    private Temp mK1;   // $27 : $k1
    // Global pointer
    private Temp mGP;   // $28 : $gp
    // Stack pointer
    private Temp mSP;   // $29 : $sp
    // Frame pointer
    private Temp mFP;   // $30 : $fp
    // Return address
    private Temp mRA;   // $31 : $ra

    private Temp[] mRegArgs;
    private int mOffsetLocals;
    private int mOffsetArgs;
    private HashMap<Temp, String> mNamesOfRegs;
    private LinkedList<Temp> mSpecialRegs;
    private LinkedList<Temp> mArgRegs;
    private LinkedList<Temp> mCalleeSavesRegs;
    private LinkedList<Temp> mCallerSavesRegs;
    private LinkedList<Temp> mReturnSink;

    public MipsFrame() {
        mZero = new Temp();
        mAT = new Temp();
        mV0 = new Temp();
        mV1 = new Temp();
        mA0 = new Temp();
        mA1 = new Temp();
        mA2 = new Temp();
        mA3 = new Temp();
        mT0 = new Temp();
        mT1 = new Temp();
        mT2 = new Temp();
        mT3 = new Temp();
        mT4 = new Temp();
        mT5 = new Temp();
        mT6 = new Temp();
        mT7 = new Temp();
        mS0 = new Temp();
        mS1 = new Temp();
        mS2 = new Temp();
        mS3 = new Temp();
        mS4 = new Temp();
        mS5 = new Temp();
        mS6 = new Temp();
        mS7 = new Temp();
        mT8 = new Temp();
        mT9 = new Temp();
        mK0 = new Temp();
        mK1 = new Temp();
        mGP = new Temp();
        mSP = new Temp();
        mFP = new Temp();
        mRA = new Temp();

        mNamesOfRegs = new HashMap<>();
        mNamesOfRegs.put(mZero, "$zero");
        mNamesOfRegs.put(mAT, "$at");
        mNamesOfRegs.put(mV0, "$v0");
        mNamesOfRegs.put(mV1, "$v1");
        mNamesOfRegs.put(mA0, "$a0");
        mNamesOfRegs.put(mA1, "$a1");
        mNamesOfRegs.put(mA2, "$a2");
        mNamesOfRegs.put(mA3, "$a3");
        mNamesOfRegs.put(mT0, "$t0");
        mNamesOfRegs.put(mT1, "$t1");
        mNamesOfRegs.put(mT2, "$t2");
        mNamesOfRegs.put(mT3, "$t3");
        mNamesOfRegs.put(mT4, "$t4");
        mNamesOfRegs.put(mT5, "$t5");
        mNamesOfRegs.put(mT6, "$t6");
        mNamesOfRegs.put(mT7, "$t7");
        mNamesOfRegs.put(mS0, "$s0");
        mNamesOfRegs.put(mS1, "$s1");
        mNamesOfRegs.put(mS2, "$s2");
        mNamesOfRegs.put(mS3, "$s3");
        mNamesOfRegs.put(mS4, "$s4");
        mNamesOfRegs.put(mS5, "$s5");
        mNamesOfRegs.put(mS6, "$s6");
        mNamesOfRegs.put(mS7, "$s7");
        mNamesOfRegs.put(mT8, "$t8");
        mNamesOfRegs.put(mT9, "$t9");
        mNamesOfRegs.put(mK0, "$k0");
        mNamesOfRegs.put(mK1, "$k1");
        mNamesOfRegs.put(mGP, "$gp");
        mNamesOfRegs.put(mSP, "$sp");
        mNamesOfRegs.put(mFP, "$fp");
        mNamesOfRegs.put(mRA, "$ra");

        mSpecialRegs = new LinkedList<>();
        mSpecialRegs.add(mZero);
        mSpecialRegs.add(mAT);
        mSpecialRegs.add(mV0);
        mSpecialRegs.add(mV1);
        mSpecialRegs.add(mK0);
        mSpecialRegs.add(mK1);
        mSpecialRegs.add(mGP);
        mSpecialRegs.add(mSP);
        mSpecialRegs.add(mFP);
        mSpecialRegs.add(mRA);

        mArgRegs = new LinkedList<>();
        mArgRegs.add(mA0);
        mArgRegs.add(mA1);
        mArgRegs.add(mA2);
        mArgRegs.add(mA3);

        mCalleeSavesRegs = new LinkedList<>();
        mCalleeSavesRegs.add(mT0);
        mCalleeSavesRegs.add(mT1);
        mCalleeSavesRegs.add(mT2);
        mCalleeSavesRegs.add(mT3);
        mCalleeSavesRegs.add(mT4);
        mCalleeSavesRegs.add(mT5);
        mCalleeSavesRegs.add(mT6);
        mCalleeSavesRegs.add(mT7);
        mCalleeSavesRegs.add(mT8);
        mCalleeSavesRegs.add(mT9);

        mCallerSavesRegs = new LinkedList<>();
        mCallerSavesRegs.add(mS0);
        mCallerSavesRegs.add(mS1);
        mCallerSavesRegs.add(mS2);
        mCallerSavesRegs.add(mS3);
        mCallerSavesRegs.add(mS4);
        mCallerSavesRegs.add(mS5);
        mCallerSavesRegs.add(mS6);
        mCallerSavesRegs.add(mS7);

        mReturnSink = new LinkedList<>();
        mReturnSink.addAll(mSpecialRegs);
        mReturnSink.addAll(mCalleeSavesRegs);

        mRegArgs = new Temp[] { mA0, mA1, mA2, mA3 };
        mFormalArguments = new ArrayList<>();
        mLocalVariables = new ArrayList<>();
        mOffsetArgs = getWordSize() * 4;
        mOffsetLocals = 0;
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

    @Override
    public LinkedList<Temp> getSpecialRegs() {
        return mSpecialRegs;
    }

    @Override
    public LinkedList<Temp> getArgRegs() {
        return mArgRegs;
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
        return mFP;
    }

    @Override
    public Temp getRV() {
        return mV0;
    }

    @Override
    public Temp getRA() {
        return mRA;
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

    @Override
    public LinkedList<AsmInstruction> procEntryExit2(LinkedList<AsmInstruction> body) {
        body.add(new AsmOPER("", null, mReturnSink));
        return body;
    }

    @Override
    public LinkedList<AsmInstruction> procEntryExit3(LinkedList<AsmInstruction> body) {
        return body;
    }

    @Override
    public LinkedList<AsmInstruction> codegen(IRStatement statement) {
        MipsCodegen mipsCodegen = new MipsCodegen(this);
        return mipsCodegen.codegen(statement);
    }

    @Override
    public String tempMap(Temp temp) {
        return mNamesOfRegs.get(temp);
    }

}
