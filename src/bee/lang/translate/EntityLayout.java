package bee.lang.translate;

import java.util.HashMap;

// This class is a list. Each node of the list contains a map of values like this: Entity ID -> Position of entity in a class.
// This information can be used to generate vtable (virtual table) for classes and represent fields of objects in memory.
public class EntityLayout {

    private HashMap<String, Integer> mPositionOfObject;
    private EntityLayout mPrevious;
    private int mIndex;

    public EntityLayout(EntityLayout previous) {
        mPositionOfObject = new HashMap<>();
        mPrevious = previous;
        mIndex = (previous == null ? 0 : previous.mIndex);
    }

    public void add(String key) {
        add(key, mIndex++);
    }

    public void add(String key, int position) {
        mPositionOfObject.put(key, position);
    }

    public int get(String key) {
        Integer position = mPositionOfObject.get(key);

        if (position == null) {
            if (mPrevious != null) {
                return mPrevious.get(key);
            }
        }

        return position != null ? position : -1;
    }

}
