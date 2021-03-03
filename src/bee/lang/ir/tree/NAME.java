package bee.lang.ir.tree;

import bee.lang.ir.Label;

public class NAME extends IRExpression {

    private Label mLabel;

    public NAME(Label label) {
        mLabel = label;
    }

    public Label getLabel() {
        return mLabel;
    }

}
