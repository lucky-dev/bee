package bee.lang.ir.tree;

import java.util.Iterator;
import java.util.LinkedList;

public class CALL extends IRExpression {

    private IRExpression mFunction;
    private LinkedList<IRExpression> mArguments;

    public CALL(IRExpression function, LinkedList<IRExpression> arguments) {
        mFunction = function;
        mArguments = arguments;
    }

    public IRExpression getFunction() {
        return mFunction;
    }

    public LinkedList<IRExpression> getArguments() {
        return mArguments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<IRExpression> iterator = mArguments.iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next());

            while (iterator.hasNext()) {
                sb.append(", ");
                sb.append(iterator.next());
            }
        }

        return "CALL(" + mFunction + ", " + sb + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        LinkedList<IRExpression> kids = new LinkedList<>();
        kids.add(mFunction);
        kids.addAll(mArguments);
        return kids;
    }

    @Override
    public IRExpression build(LinkedList<IRExpression> kids) {
        return new CALL(kids.removeFirst(), kids);
    }

}
