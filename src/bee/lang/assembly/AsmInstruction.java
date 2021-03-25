package bee.lang.assembly;

import bee.lang.ir.Label;
import bee.lang.ir.Temp;

import java.util.Iterator;
import java.util.LinkedList;

public abstract class AsmInstruction {

    protected String mInstruction;

    public abstract LinkedList<Temp> getUse();
    public abstract LinkedList<Temp> getDef();
    public abstract LinkedList<Label> getJumps();

    public String format(TempMap tempMap) {
        LinkedList<Temp> srcRegs = getUse();
        LinkedList<Temp> dstRegs = getDef();

        if (srcRegs != null) {
            Iterator<Temp> iterator = srcRegs.iterator();

            int i = 0;
            while (iterator.hasNext()) {
                Temp temp = iterator.next();

                String value = tempMap.tempMap(temp);

                if (value == null) {
                    value = temp.toString();
                }

                mInstruction = mInstruction.replace("%s" + i, value);

                i++;
            }
        }

        if (dstRegs != null) {
            Iterator<Temp> iterator = dstRegs.iterator();

            int i = 0;
            while (iterator.hasNext()) {
                Temp temp = iterator.next();

                String value = tempMap.tempMap(temp);

                if (value == null) {
                    value = temp.toString();
                }

                mInstruction = mInstruction.replace("%d" + i, value);

                i++;
            }
        }

        return mInstruction;
    }

}
