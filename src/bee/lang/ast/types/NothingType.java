package bee.lang.ast.types;

public class NothingType extends BaseType {

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        return this == type;
    }

    @Override
    public boolean isNothing() {
        return true;
    }

}
