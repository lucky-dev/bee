package bee.lang.ir;

import java.util.HashMap;

public class Label {

    private static HashMap<String, Label> sLabels = new HashMap<>();
    private static int sCount = 0;

    private String mName;

    private Label(String name) {
        mName = name;
    }

    private Label() {
        this("L" + sCount++);
    }

    public String getName() {
        return mName;
    }

    public static Label newLabel() {
        return new Label();
    }

    public static Label newLabel(String name) {
        Label label = sLabels.get(name);

        if (label == null) {
            label = new Label(name);
            sLabels.put(name, label);
        }

        return label;
    }

    @Override
    public String toString() {
        return mName;
    }

}
