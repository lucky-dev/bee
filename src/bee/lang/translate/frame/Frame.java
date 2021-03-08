package bee.lang.translate.frame;

import bee.lang.ir.Label;
import bee.lang.ir.Temp;

public abstract class Frame {

    public abstract Frame newFrame(Label name, boolean... args);
    public abstract Access allocLocal(boolean isInFrame);
    public abstract int getWordSize();
    public abstract Temp getFP();
    public abstract Temp getFirstArg();

}
