package bee.lang.translate;

public class StringFragment extends DataFragment {

    private String mLabel;
    private String mStr;

    public StringFragment(String label, String str) {
        mLabel = label;
        mStr = str;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getStr() {
        return mStr;
    }

    @Override
    public String toString() {
        return mLabel + ": " + mStr;
    }

}
