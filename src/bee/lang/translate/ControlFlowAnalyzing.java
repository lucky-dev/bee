package bee.lang.translate;

import bee.lang.ir.Label;
import bee.lang.ir.tree.*;

import java.util.Iterator;
import java.util.LinkedList;

// This class is used to create basic blocks and analyze them. Basic blocks may have any order. It gives an opportunity to optimize code.
public class ControlFlowAnalyzing {

    private String mMethodName;
    private Iterator<IRStatement> mIteratorStatement;

    public ControlFlowAnalyzing(String methodName) {
        mMethodName = methodName;
    }

    public LinkedList<BasicBlock> createBasicBlocks(LinkedList<IRStatement> statements) {
        LinkedList<BasicBlock> basicBlocks = new LinkedList<>();

        if (statements.isEmpty()) {
            return basicBlocks;
        }

        BasicBlock basicBlock = new BasicBlock();
        basicBlocks.add(basicBlock);

        for (IRStatement statement : statements) {
            if (basicBlock.isEmpty()) {
                if (!(statement instanceof LABEL)) {
                    basicBlock.addStatement(new LABEL(Label.newLabel()));
                }

                basicBlock.addStatement(statement);
            } else {
                if (statement instanceof LABEL) {
                    basicBlock.addStatement(new JUMP(((LABEL) statement).getLabel()));
                    basicBlock = new BasicBlock();
                    basicBlocks.add(basicBlock);
                    basicBlock.addStatement(statement);
                } else if ((statement instanceof CJUMP) || (statement instanceof JUMP)) {
                    basicBlock.addStatement(statement);
                    basicBlock = new BasicBlock();
                    basicBlocks.add(basicBlock);
                } else {
                    basicBlock.addStatement(statement);
                }
            }
        }

        if (basicBlock.isEmpty()) {
            basicBlock.addStatement(new LABEL(Label.newLabel()));
        }

        basicBlock.addStatement(new JUMP(Label.newLabel("_" + mMethodName + "_end_")));

        return basicBlocks;
    }

    // This analysis follows a simple rules:
    // 1. CJUMP followed by its label 'false' (need to rewrite CJUMP).
    // 2. JUMP must be removed if JUMP followed by its label.
    public LinkedList<IRStatement> trace(LinkedList<IRStatement> irStatements) {
        Iterator<BasicBlock> basicBlocksIterator = createBasicBlocks(irStatements).iterator();

        LinkedList<IRStatement> statements = new LinkedList<>();

        IRStatement statement;
        IRStatement prevStatement = null;
        while ((statement = getNextStatement(basicBlocksIterator)) != null) {
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

    private IRStatement getNextStatement(Iterator<BasicBlock> mIteratorBasicBlock) {
        if ((mIteratorStatement == null) || (!mIteratorStatement.hasNext())) {
            if (mIteratorBasicBlock.hasNext()) {
                mIteratorStatement = mIteratorBasicBlock.next().getStatements().iterator();
            } else {
                return null;
            }
        }

        return mIteratorStatement.next();
    }

}
