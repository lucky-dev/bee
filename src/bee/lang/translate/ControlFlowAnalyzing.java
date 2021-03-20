package bee.lang.translate;

import bee.lang.ir.Label;
import bee.lang.ir.tree.*;

import java.util.Iterator;
import java.util.LinkedList;

// This class is used to create basic blocks and analyze them. Basic blocks may have any order. It gives an opportunity to optimize code.
public class ControlFlowAnalyzing {

    private LinkedList<BasicBlock> mBasicBlocks;
    private Iterator<BasicBlock> mIteratorBasicBlock;
    private Iterator<IRStatement> mIteratorStatement;
    private BasicBlock mBasicBlock;

    public ControlFlowAnalyzing() {
        mBasicBlocks = new LinkedList<>();
    }

    public void createBasicBlocks(String methodName, LinkedList<IRStatement> statements) {
        if (statements.isEmpty()) {
            return;
        }

        BasicBlock basicBlock = new BasicBlock();
        mBasicBlocks.add(basicBlock);

        for (IRStatement statement : statements) {
            if (basicBlock.isEmpty()) {
                if (statement instanceof LABEL) {
                    basicBlock.addStatement(statement);
                } else {
                    basicBlock.addStatement(new LABEL(Label.newLabel()));
                    basicBlock.addStatement(statement);
                }
            } else {
                if (statement instanceof LABEL) {
                    basicBlock.addStatement(new JUMP(((LABEL) statement).getLabel()));
                    basicBlock = new BasicBlock();
                    mBasicBlocks.add(basicBlock);
                    basicBlock.addStatement(statement);
                } else if ((statement instanceof CJUMP) || (statement instanceof JUMP)) {
                    basicBlock.addStatement(statement);
                    basicBlock = new BasicBlock();
                    mBasicBlocks.add(basicBlock);
                } else {
                    basicBlock.addStatement(statement);
                }
            }
        }

        if (basicBlock.isEmpty()) {
            basicBlock.addStatement(new LABEL(Label.newLabel()));
        }

        basicBlock.addStatement(new JUMP(Label.newLabel("_" + methodName + "_end_")));
    }

    public LinkedList<BasicBlock> getBasicBlocks() {
        return mBasicBlocks;
    }

    // This analysis follows a simple rules:
    // 1. CJUMP followed by its label 'false' (need to rewrite CJUMP).
    // 2. JUMP must be removed if JUMP followed by its label.
    public LinkedList<IRStatement> traceBasicBlocks() {
        LinkedList<IRStatement> statements = new LinkedList<>();

        IRStatement statement;
        IRStatement prevStatement = null;
        while ((statement = getNextStatement()) != null) {
            if (prevStatement != null) {
                if (prevStatement instanceof CJUMP) {
                    CJUMP cjump = (CJUMP) prevStatement;
                    Label trueLabel = cjump.getLblTrue();
                    Label falseLabel = cjump.getLblFalse();

                    if (statement instanceof LABEL) {
                        String nameLabel = ((LABEL) statement).getLabel().getName();

                        if (falseLabel.getName().equals(nameLabel)) {
                            statements.add(statement);
                        } else if (trueLabel.getName().equals(nameLabel)) {
                            cjump.setTypeRelOp(cjump.getTypeRelOp().negate());
                            cjump.setLblTrue(falseLabel);
                            cjump.setLblFalse(trueLabel);
                            statements.add(statement);
                        } else {
                            Label newFalseLabel = Label.newLabel();
                            cjump.setLblFalse(newFalseLabel);
                            statements.add(statement);
                            statements.add(new LABEL(newFalseLabel));
                            statements.add(new JUMP(falseLabel));
                        }
                    } else {
                        statements.add(statement);
                    }
                } else if (prevStatement instanceof JUMP) {
                    Label jumpLabel = ((NAME) ((JUMP) prevStatement).getExpression()).getLabel();

                    if (statement instanceof LABEL) {
                        if (jumpLabel.getName().equals(((LABEL) statement).getLabel().getName())) {
                            statements.removeLast();
                        }
                    }

                    statements.add(statement);
                } else {
                    statements.add(statement);
                }
            } else {
                statements.add(statement);
            }

            prevStatement = statement;
        }

        return statements;
    }

    private IRStatement getNextStatement() {
        if (mIteratorBasicBlock == null) {
            mIteratorBasicBlock = mBasicBlocks.iterator();
        }

        if ((mIteratorStatement == null) || (!mIteratorStatement.hasNext())) {
            if (mIteratorBasicBlock.hasNext()) {
                mBasicBlock = mIteratorBasicBlock.next();
                mIteratorStatement = mBasicBlock.getStatements().iterator();
            } else {
                return null;
            }
        }

        return mIteratorStatement.next();
    }

}
