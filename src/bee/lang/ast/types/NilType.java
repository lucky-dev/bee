package bee.lang.ast.types;

public class NilType extends BaseType {

    @Override
    public boolean isEqual(BaseType type) {
        if (type == null) {
            return false;
        }

        return this == type;
    }

    @Override
    public boolean isNil() {
        return true;
    }

}
