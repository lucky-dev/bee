package bee.lang.ir.tree;

import bee.lang.ir.Label;

import java.util.LinkedList;

public class NAME extends IRExpression {

    private Label mLabel;

    public NAME(Label label) {
        mLabel = label;
    }

    public Label getLabel() {
        return mLabel;
    }

    @Override
    public String toString() {
        return "NAME(" + mLabel + ")";
    }

    @Override
    public LinkedList<IRExpression> kids() {
        return null;
    }

    @Override
    public IRExpression build(LinkedList<IRExpression> kids) {
        return this;
    }

}
