package bee.lang.ast.types;

import java.util.Iterator;
import java.util.LinkedList;

public class MethodType extends BaseType {

    private LinkedList<BaseType> mTypes;
    private BaseType mReturnType;

    public MethodType() {
        mTypes = new LinkedList<>();
    }

    public void addFormalArgumentType(BaseType type) {
        mTypes.add(type);
    }

    public void addReturnType(BaseType returnType) {
        mReturnType = returnType;
    }

    public BaseType getReturnType() {
        return mReturnType;
    }

    public boolean isEqualFormalArguments(MethodType otherType) {
        if (mTypes.size() != otherType.mTypes.size()) {
            return false;
        }

        Iterator<BaseType> iteratorThisType = mTypes.iterator();
        Iterator<BaseType> iteratorOtherType = otherType.mTypes.iterator();

        while ((iteratorThisType.hasNext()) && (iteratorOtherType.hasNext())) {
            if (!iteratorThisType.next().isEqual(iteratorOtherType.next())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        if (this == type) {
            return true;
        }

        if (!(type instanceof MethodType)) {
            return false;
        }

        MethodType otherType = (MethodType) type;

        if (mTypes.size() != otherType.mTypes.size()) {
            return false;
        }

        Iterator<BaseType> iteratorThisType = mTypes.iterator();
        Iterator<BaseType> iteratorOtherType = otherType.mTypes.iterator();

        while ((iteratorThisType.hasNext()) && (iteratorOtherType.hasNext())) {
            if (!iteratorThisType.next().isEqual(iteratorOtherType.next())) {
                return false;
            }
        }

        if (!getReturnType().isEqual(otherType.getReturnType())) {
            return false;
        }

        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        Iterator<BaseType> iteratorTypes = mTypes.iterator();

        if (iteratorTypes.hasNext()) {
            sb.append(iteratorTypes.next());
        }

        while (iteratorTypes.hasNext()) {
            sb.append(", ");
            sb.append(iteratorTypes.next());
        }

        if (!mReturnType.isEqual(Type.Void)) {
            sb.append(" -> ");
            sb.append(mReturnType);
        }

        return sb.toString();
    }

    @Override
    public boolean isMethod() {
        return true;
    }

}
