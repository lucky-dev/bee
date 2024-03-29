package bee.lang.assembly;

import bee.lang.exceptions.CodegenException;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.*;
import bee.lang.translate.frame.Frame;

import java.util.Iterator;
import java.util.LinkedList;

public class MipsCodegen {

    private Frame mFrame;
    private LinkedList<AsmInstruction> mInstructionsList;

    public MipsCodegen(Frame frame) {
        mFrame = frame;
        mInstructionsList = new LinkedList<>();
    }

    public LinkedList<AsmInstruction> codegen(IRStatement statement) throws CodegenException {
        munchStatement(statement);
        return mInstructionsList;
    }

    private void munchStatement(IRStatement statement) throws CodegenException {
        if (statement instanceof MOVE) {
            MOVE move = (MOVE) statement;
            if (move.getDst() instanceof MEM) {
                MEM mem = (MEM) move.getDst();
                if (mem.getExpression() instanceof BINOP) {
                    BINOP binop = (BINOP) mem.getExpression();
                    if (binop.getTypeBinOp() == TypeBinOp.PLUS) {
                        if (binop.getRightExpression() instanceof CONST) {
                            // MOVE(MEM(BINOP(PLUS, e1, CONST(n))), e2)
                            mInstructionsList.add(new AsmOPER("sw %s0, " + ((CONST) binop.getRightExpression()).getValue() + "(%s1)", emptyList(), list(munchExpression(move.getSrc()), munchExpression(binop.getLeftExpression()))));
                            return;
                        } else if (binop.getLeftExpression() instanceof CONST) {
                            // MOVE(MEM(BINOP(PLUS, CONST(n), e1)), e2)
                            mInstructionsList.add(new AsmOPER("sw %s0, " + ((CONST) binop.getLeftExpression()).getValue() + "(%s1)", emptyList(), list(munchExpression(move.getSrc()), munchExpression(binop.getRightExpression()))));
                            return;
                        }
                    }
                }

                // MOVE(MEM(e1), e2)
                mInstructionsList.add(new AsmOPER("sw %s0, 0(%s1)", emptyList(), list(munchExpression(move.getSrc()), munchExpression(mem.getExpression()))));
                return;
            }

            if (move.getDst() instanceof TEMP) {
                if (move.getSrc() instanceof CALL) {
                    // MOVE(TEMP(t), CALL(f, args))
                    handleCall((CALL) move.getSrc());
                    mInstructionsList.add(new AsmMOVE("move %d0, %s0", ((TEMP) move.getDst()).getTemp(), mFrame.getRV()));
                } else {
                    // MOVE(TEMP(t), e1)
                    mInstructionsList.add(new AsmMOVE("move %d0, %s0", ((TEMP) move.getDst()).getTemp(), munchExpression(move.getSrc())));
                }

                return;
            }
        }

        if (statement instanceof LABEL) {
            LABEL label = (LABEL) statement;
            mInstructionsList.add(new AsmLABEL(label.getLabel().getName() + ":", label.getLabel()));
            return;
        }

        if (statement instanceof CJUMP) {
            CJUMP cjump = (CJUMP) statement;

            String relOp;

            switch (cjump.getTypeRelOp()) {
                case EQ: {
                    relOp = "beq";
                } break;

                case NE: {
                    relOp = "bne";
                } break;

                case LT: {
                    relOp = "blt";
                } break;

                case GT: {
                    relOp = "bgt";
                } break;

                case LE: {
                    relOp = "ble";
                } break;

                default: {
                    relOp = "bge";
                }
            }

            // CJUMP(op, e1, e2, t, f)
            mInstructionsList.add(new AsmOPER(relOp + " %s0, %s1, " + cjump.getLblTrue(), emptyList(), list(munchExpression(cjump.getLeftExpression()), munchExpression(cjump.getRightExpression())), list(cjump.getLblTrue())));
            return;
        }

        if (statement instanceof JUMP) {
            // JUMP(l)
            JUMP jump = (JUMP) statement;
            mInstructionsList.add(new AsmOPER("b " + jump.getTargets().getFirst(), emptyList(), emptyList(), jump.getTargets()));
            return;
        }

        // Ignore EXP(TEMP(t)) and EXP(CONST(n))
        if ((statement instanceof EXP) && ((((EXP) statement).getExpression() instanceof TEMP) || (((EXP) statement).getExpression() instanceof CONST))) {
            return;
        }

        if ((statement instanceof EXP) && (((EXP) statement).getExpression() instanceof CALL)) {
            // EXP(CALL(f, args))
            handleCall((CALL) ((EXP) statement).getExpression());
            return;
        }

        throw new CodegenException(statement);
    }

    private Temp munchExpression(IRExpression expression) throws CodegenException {
        if (expression instanceof MEM) {
            MEM mem = (MEM) expression;
            if (mem.getExpression() instanceof BINOP) {
                BINOP binop = (BINOP) mem.getExpression();
                if (binop.getTypeBinOp() == TypeBinOp.PLUS) {
                    if (binop.getRightExpression() instanceof CONST) {
                        // MEM(BINOP(PLUS, e1, CONST(n)))
                        Temp result = new Temp();
                        mInstructionsList.add(new AsmOPER("lw %d0, " + ((CONST) binop.getRightExpression()).getValue() + "(%s0)", list(result), list(munchExpression(binop.getLeftExpression()))));
                        return result;
                    } else if (binop.getLeftExpression() instanceof CONST) {
                        // MEM(BINOP(PLUS, CONST(n), e1))
                        Temp result = new Temp();
                        mInstructionsList.add(new AsmOPER("lw %d0, " + ((CONST) binop.getLeftExpression()).getValue() + "(%s0)", list(result), list(munchExpression(binop.getRightExpression()))));
                        return result;
                    }
                }
            }

            // MEM(e1)
            Temp result = new Temp();
            mInstructionsList.add(new AsmOPER("lw %d0, 0(%s0)", list(result), list(munchExpression(mem.getExpression()))));
            return result;
        }

        if (expression instanceof BINOP) {
            BINOP binop = (BINOP) expression;
            Temp result;
            switch (binop.getTypeBinOp()) {
                case PLUS: {
                    if (binop.getRightExpression() instanceof CONST) {
                        // BINOP(PLUS, e1, CONST(n))
                        result = new Temp();
                        mInstructionsList.add(new AsmOPER("addiu %d0, %s0, " + ((CONST) binop.getRightExpression()).getValue(), list(result), list(munchExpression(binop.getLeftExpression()))));
                    } else if (binop.getLeftExpression() instanceof CONST) {
                        // BINOP(PLUS, CONST(n), e1)
                        result = new Temp();
                        mInstructionsList.add(new AsmOPER("addiu %d0, %s0, " + ((CONST) binop.getLeftExpression()).getValue(), list(result), list(munchExpression(binop.getRightExpression()))));
                    } else {
                        // BINOP(PLUS, e1, e2)
                        result = new Temp();
                        mInstructionsList.add(new AsmOPER("addu %d0, %s0, %s1", list(result), list(munchExpression(binop.getLeftExpression()), munchExpression(binop.getRightExpression()))));
                    }
                } break;

                case MINUS: {
                    // BINOP(MINUS, e1, e2)
                    result = new Temp();
                    mInstructionsList.add(new AsmOPER("sub %d0, %s0, %s1", list(result), list(munchExpression(binop.getLeftExpression()), munchExpression(binop.getRightExpression()))));
                } break;

                case MUL: {
                    // BINOP(MUL, e1, e2)
                    result = new Temp();
                    mInstructionsList.add(new AsmOPER("mult %s0, %s1", emptyList(), list(munchExpression(binop.getLeftExpression()), munchExpression(binop.getRightExpression()))));
                    mInstructionsList.add(new AsmOPER("mflo %d0", list(result), emptyList()));
                } break;

                case DIV: {
                    // BINOP(DIV, e1, e2)
                    result = new Temp();
                    mInstructionsList.add(new AsmOPER("div %s0, %s1", emptyList(), list(munchExpression(binop.getLeftExpression()), munchExpression(binop.getRightExpression()))));
                    mInstructionsList.add(new AsmOPER("mflo %d0", list(result), emptyList()));
                } break;

                default: {
                    // BINOP(MOD, e1, e2)
                    result = new Temp();
                    mInstructionsList.add(new AsmOPER("div %s0, %s1", emptyList(), list(munchExpression(binop.getLeftExpression()), munchExpression(binop.getRightExpression()))));
                    mInstructionsList.add(new AsmOPER("mfhi %d0", list(result), emptyList()));
                }
            }

            return result;
        }

        if (expression instanceof CONST) {
            // CONST(n)
            Temp result = new Temp();
            mInstructionsList.add(new AsmOPER("li %d0, " + ((CONST) expression).getValue(), list(result), emptyList()));
            return result;
        }

        if (expression instanceof NAME) {
            // NAME(l)
            Temp result = new Temp();
            mInstructionsList.add(new AsmOPER("la %d0, " + ((NAME) expression).getLabel().getName(), list(result), emptyList()));
            return result;
        }

        if (expression instanceof TEMP) {
            return ((TEMP) expression).getTemp();
        }

        throw new CodegenException(expression);
    }

    private void handleCall(CALL call) throws CodegenException {
        LinkedList<Temp> calldefs = new LinkedList<>();
        calldefs.add(mFrame.getRA());
        calldefs.add(mFrame.getRV());
        calldefs.add(mFrame.getFP());
        calldefs.addAll(mFrame.getCallerSavesRegs());
        LinkedList<Temp> args = new LinkedList<>();
        args.add(munchExpression(call.getFunction()));
        args.addAll(munchArgs(call.getArguments()));
        mInstructionsList.add(new AsmOPER("jal %s0", calldefs, args));
    }

    private LinkedList<Temp> munchArgs(LinkedList<IRExpression> args) throws CodegenException {
        LinkedList<Temp> result = new LinkedList<>();

        int i = 0;
        Iterator<Temp> argRegsIterator = mFrame.getArgRegs().iterator();
        for (IRExpression expression : args) {
            Temp temp = munchExpression(expression);

            if (argRegsIterator.hasNext()) {
                mInstructionsList.add(new AsmMOVE("move %d0, %s0", argRegsIterator.next(), temp));
            } else {
                mInstructionsList.add(new AsmOPER("sw %s0, " + (i * mFrame.getWordSize()) + "(%s1)", emptyList(), list(temp, mFrame.getFP())));
                i++;
            }

            result.add(temp);
        }

        return result;
    }

    private <T> LinkedList<T> list(T... items) {
        LinkedList<T> list = new LinkedList<>();

        for (T item : items) {
            list.add(item);
        }

        return list;
    }

    private <T> LinkedList<T> emptyList() {
        return new LinkedList<>();
    }

}
