package bee.lang.ast.types;

import bee.lang.ast.Identifier;

public class ClassType extends BaseType {

    private Identifier mIdentifier;
    private ClassType mBaseClass;

    public ClassType(Identifier identifier) {
        mIdentifier = identifier;
    }

    public Identifier getIdentifier() {
        return mIdentifier;
    }

    public void setBaseClass(ClassType baseClass) {
        mBaseClass = baseClass;
    }

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        return this == type;
    }

    public String toString() {
        return mIdentifier.getName();
    }

    @Override
    public boolean isClass() {
        return true;
    }

    public ClassType lub(ClassType type) {
        ClassType otherClassType = type;

        while (otherClassType != null) {
            ClassType currentClassType = this;

            while (currentClassType != null) {
                if (currentClassType.isEqual(otherClassType)) {
                    return currentClassType;
                }

                currentClassType = currentClassType.mBaseClass;
            }

            otherClassType = otherClassType.mBaseClass;
        }

        return null;
    }

    public boolean isSubclassOf(ClassType type) {
        ClassType baseClassType = this;

        while (baseClassType != null) {
            if (baseClassType.isEqual(type)) {
                return true;
            }

            baseClassType = baseClassType.mBaseClass;
        }

        return false;
    }

}
