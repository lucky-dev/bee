package bee.lang.ast.types;

public class CharType extends BaseType {

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        return this == type;
    }

    public String toString() {
        return "char";
    }

    @Override
    public boolean isChar() {
        return true;
    }

}
