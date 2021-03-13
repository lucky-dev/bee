package bee.lang.ir.tree;

import bee.lang.ir.Label;

public class LABEL extends IRStatement {

    private Label mLabel;

    public LABEL(Label label) {
        mLabel = label;
    }

    @Override
    public String toString() {
        return "LABEL(" + mLabel.toString() + ")";
    }

}
