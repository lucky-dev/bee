package bee.lang.ast.types;

public class BoolType extends BaseType {

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        return this == type;
    }

    public String toString() {
        return "bool";
    }

    @Override
    public boolean isBool() {
        return true;
    }

}
