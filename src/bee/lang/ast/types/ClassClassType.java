package bee.lang.ast.types;

public class ClassClassType extends BaseType {

    private ClassType mClassType;

    public ClassClassType(ClassType classType) {
        mClassType = classType;
    }

    public ClassType getClassType() {
        return mClassType;
    }

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        if (this == type) {
            return true;
        }

        if (!(type instanceof ClassClassType)) {
            return false;
        }

        ClassClassType otherType = (ClassClassType) type;

        return mClassType.isEqual(otherType.mClassType);
    }

    @Override
    public boolean isClassClass() {
        return true;
    }

}
