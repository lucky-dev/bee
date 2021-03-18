package bee.lang.ir.tree;

import bee.lang.ir.Label;

import java.util.Arrays;
import java.util.Iterator;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Iterator<Label> iterator = mTargets.iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next());

            while (iterator.hasNext()) {
                sb.append(", ");
                sb.append(iterator.next());
            }
        }

        return "JUMP(" + mExpression + ", " + sb + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        LinkedList<IRExpression> kids = new LinkedList<>();
        kids.add(mExpression);
        return kids;
    }

    @Override
    public IRStatement build(LinkedList<IRExpression> kids) {
        return new JUMP(kids.getFirst(), mTargets);
    }

}
