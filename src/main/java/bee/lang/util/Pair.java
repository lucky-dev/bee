package bee.lang.util;

public class Pair<T1, T2> {

    private T1 mFirst;
    private T2 mSecond;

    public Pair(T1 first, T2 second) {
        mFirst = first;
        mSecond = second;
    }

    public T1 getFirst() {
        return mFirst;
    }

    public T2 getSecond() {
        return mSecond;
    }

}
