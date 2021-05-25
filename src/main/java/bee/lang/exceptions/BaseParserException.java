package bee.lang.exceptions;

import bee.lang.lexer.Token;

public class BaseParserException extends Exception {

    protected Token mToken;

    public BaseParserException(Token token) {
        mToken = token;
    }

}
