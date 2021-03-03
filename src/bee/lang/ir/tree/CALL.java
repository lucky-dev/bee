package bee.lang.ir.tree;

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

}
