package bee.lang.ast.types;

public class ArrayType extends BaseType {

    private BaseType mType;

    public ArrayType(BaseType type) {
        mType = type;
    }

    public BaseType getType() {
        return mType;
    }

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        if (this == type) {
            return true;
        }

        if (!(type instanceof ArrayType)) {
            return false;
        }

        ArrayType otherType = (ArrayType) type;

        return mType.isEqual(otherType.mType);
    }

    public String toString() {
        return mType + "[]";
    }

    @Override
    public boolean isArray() {
        return true;
    }

}
