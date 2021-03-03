package bee.lang.ir.tree;

import bee.lang.ir.Temp;

public class TEMP extends IRExpression {

    private Temp mTemp;

    public TEMP(Temp temp) {
        mTemp = temp;
    }

    public Temp getTemp() {
        return mTemp;
    }

}
