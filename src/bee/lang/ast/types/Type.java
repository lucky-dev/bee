package bee.lang.ast.types;

import bee.lang.ast.Identifier;

import java.util.HashMap;
import java.util.Set;

public class Type {

    public static final IntType Int = new IntType();
    public static final BoolType Bool = new BoolType();
    public static final CharType Char = new CharType();
    public static final ErrorType Error = new ErrorType();
    public static final NothingType Nothing = new NothingType();
    public static final VoidType Void = new VoidType();
    public static final NilType Nil = new NilType();

    private static HashMap<String, ClassType> sClassTypes = new HashMap<>();

    public static ClassType Class(Identifier identifier) {
        return Class(identifier.getName());
    }

    public static ClassType Class(String className) {
        return sClassTypes.get(className);
    }

    public static ClassType defineClassType(Identifier identifier) {
        ClassType classType = new ClassType(identifier);
        sClassTypes.put(identifier.getName(), classType);

        return classType;
    }

    public static Set<String> getDefinedClassesNames() {
        return sClassTypes.keySet();
    }

    private Type() {
    }

}
