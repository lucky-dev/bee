package bee.lang.ir.tree;

import bee.lang.ir.Temp;

import java.util.LinkedList;

public class TEMP extends IRExpression {

    private Temp mTemp;

    public TEMP(Temp temp) {
        mTemp = temp;
    }

    public Temp getTemp() {
        return mTemp;
    }

    @Override
    public String toString() {
        return "TEMP(" + mTemp + ")";
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
