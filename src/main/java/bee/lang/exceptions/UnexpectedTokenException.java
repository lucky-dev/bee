package bee.lang.exceptions;

import bee.lang.lexer.Token;

public class UnexpectedTokenException extends BaseParserException {

    public UnexpectedTokenException(Token token) {
        super(token);
    }

    public String toString() {
        return "[" + mToken.getFileName() + " : " + mToken.getLine() + "] Unexpected token '" + mToken.getTokenType() + "'.";
    }

}
