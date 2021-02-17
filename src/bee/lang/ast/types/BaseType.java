package bee.lang.ast.types;

public abstract class BaseType {

    public abstract boolean isEqual(BaseType type);

    public boolean isArray() {
        return false;
    }

    public boolean isBool() {
        return false;
    }

    public boolean isChar() {
        return false;
    }

    public boolean isClass() {
        return false;
    }

    public boolean isError() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isMethod() {
        return false;
    }

    public boolean isNothing() {
        return false;
    }

    public boolean isVoid() {
        return false;
    }

    public boolean isNil() {
        return false;
    }

}
