package bee.lang.ir.tree;

import bee.lang.ir.Label;

import java.util.LinkedList;

public class LABEL extends IRStatement {

    private Label mLabel;

    public LABEL(Label label) {
        mLabel = label;
    }

    @Override
    public String toString() {
        return "LABEL(" + mLabel.toString() + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        return null;
    }

    @Override
    public IRStatement build(LinkedList<IRExpression> kids) {
        return this;
    }

}
