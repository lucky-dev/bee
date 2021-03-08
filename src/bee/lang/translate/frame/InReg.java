package bee.lang.translate.frame;

import bee.lang.ir.Temp;
import bee.lang.ir.tree.IRExpression;
import bee.lang.ir.tree.TEMP;

public class InReg extends Access {

    private Temp mTemp;

    public InReg(Temp temp) {
        mTemp = temp;
    }

    public Temp getTemp() {
        return mTemp;
    }

    @Override
    public IRExpression exp(IRExpression fp) {
        return new TEMP(mTemp);
    }

}
