package bee.lang.ast.types;

public class ErrorType extends BaseType {

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        return this == type;
    }

    @Override
    public boolean isError() {
        return true;
    }

    public String toString() {
        return "error";
    }

}
