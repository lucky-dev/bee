package bee.lang.ir.tree;

import bee.lang.ir.Label;

import java.util.Arrays;
import java.util.LinkedList;

public class JUMP extends IRStatement {

    private IRExpression mExpression;
    private LinkedList<Label> mTargets;

    public JUMP(IRExpression expression, LinkedList<Label> targets) {
        mExpression = expression;
        mTargets = targets;
    }

    public JUMP(Label target) {
        this(new NAME(target), new LinkedList<Label>(Arrays.asList(target)));
    }

    public IRExpression getExpression() {
        return mExpression;
    }

    public LinkedList<Label> getTargets() {
        return mTargets;
    }

}
