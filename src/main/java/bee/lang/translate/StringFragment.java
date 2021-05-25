package bee.lang.translate;

public class StringFragment extends DataFragment {

    private String mLabel;
    private String mStr;

    public StringFragment() {
        this("", "");
    }

    public StringFragment(String label, String str) {
        super(label + ": " + str);

        mLabel = label;
        mStr = str;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getStr() {
        return mStr;
    }

}
