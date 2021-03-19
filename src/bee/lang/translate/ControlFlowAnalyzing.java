package bee.lang.translate;

import bee.lang.ir.Label;
import bee.lang.ir.tree.*;

import java.util.LinkedList;

public class ControlFlowAnalyzing {

    private LinkedList<BasicBlock> mBasicBlocks;

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

}
