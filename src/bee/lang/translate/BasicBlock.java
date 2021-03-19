package bee.lang.translate;

import bee.lang.ir.tree.IRStatement;

import java.util.LinkedList;

public class BasicBlock {

    private LinkedList<IRStatement> mListStatements;

    public BasicBlock() {
        mListStatements = new LinkedList<>();
    }

    public void addStatement(IRStatement statement) {
        mListStatements.add(statement);
    }

    public IRStatement getLastStatement() {
        return mListStatements.getLast();
    }

    public boolean isEmpty() {
        return mListStatements.isEmpty();
    }

    @Override
    public String toString() {
        return mListStatements.toString();
    }

}
