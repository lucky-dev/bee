package bee.lang.ast.types;

public class VoidType extends BaseType {

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        return this == type;
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    public String toString() {
        return "void";
    }

}
