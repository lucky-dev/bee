package bee.lang.translate;

public class DataFragment extends Fragment {

    private String mData;

    public DataFragment(String data) {
        mData = data;
    }

    public String getData() {
        return mData;
    }

    @Override
    public String toString() {
        return mData;
    }

}
