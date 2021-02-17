package bee.lang.ast.types;

public class IntType extends BaseType {

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        return this == type;
    }

    public String toString() {
        return "int";
    }

    @Override
    public boolean isInt() {
        return true;
    }

}
