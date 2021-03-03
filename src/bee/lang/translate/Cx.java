package bee.lang.translate;

import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.*;

public abstract class Cx extends WrapperIRExpression {

    public IRExpression unEx() {
        Temp result = new Temp();
        Label lblTrue = Label.newLabel();
        Label lblFalse = Label.newLabel();

        return new ESEQ(
                new SEQ(new MOVE(new TEMP(result), new CONST(1)),
                        new SEQ(unCx(lblTrue, lblFalse),
                                new SEQ(new LABEL(lblFalse),
                                        new SEQ(new MOVE(new TEMP(result), new CONST(0)),
                                                new LABEL(lblTrue))))),
                new TEMP(result));
    }

}
