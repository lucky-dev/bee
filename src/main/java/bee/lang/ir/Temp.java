package bee.lang.ir;

public class Temp {

    private static int sCount = 0;

    private int mIndex;

    public Temp() {
        mIndex = sCount++;
    }

    @Override
    public String toString() {
        return "t" + mIndex;
    }

}
