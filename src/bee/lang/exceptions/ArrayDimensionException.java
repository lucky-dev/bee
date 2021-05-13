package bee.lang.exceptions;

import bee.lang.lexer.Token;

public class ArrayDimensionException extends BaseParserException {

    public ArrayDimensionException(Token token) {
        super(token);
    }

    public String toString() {
        return "[" + mToken.getFileName() + " : " + mToken.getLine() + "] Compiler does not support multidimensional arrays.";
    }

}
