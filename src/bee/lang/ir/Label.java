package bee.lang.ir;

import java.util.HashMap;

public class Label {

    private static HashMap<String, Label> sLabels = new HashMap<>();
    private static HashMap<String, String> sLabelsForStrings = new HashMap<>();
    private static int sCount = 0;

    private String mName;

    private Label(String name) {
        mName = name;
    }

    private Label() {
        this("_L" + getId() + "_");
    }

    private static int getId() {
        return sCount++;
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

    public static Label newLabelForString(String str) {
        String lblName = sLabelsForStrings.get(str);
        if (lblName != null) {
            return sLabels.get(lblName);
        } else {
            Label label = new Label();
            label.mName = "_str" + label.mName;
            sLabelsForStrings.put(str, label.mName);
            sLabels.put(label.mName, label);
            return label;
        }
    }

    public static boolean isLabelForString(String str) {
        return sLabelsForStrings.containsKey(str);
    }

    @Override
    public String toString() {
        return mName;
    }

}
