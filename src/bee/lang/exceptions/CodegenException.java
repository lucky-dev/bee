package bee.lang.exceptions;

import bee.lang.ir.tree.IRExpression;
import bee.lang.ir.tree.IRStatement;

public class CodegenException extends Exception {

    public CodegenException(IRStatement statement) {
        super("There is no pattern for the tree: " + statement);
    }

    public CodegenException(IRExpression expression) {
        super("There is no pattern for the tree: " + expression);
    }

}
