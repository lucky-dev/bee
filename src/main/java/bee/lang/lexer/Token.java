package bee.lang.lexer;

public class Token {

    private TokenType mTokenType;
    private String mValue;
    private int mLine;
    private String mFileName;

    public Token(TokenType tokenType, String value, int line, String fileName) {
        mTokenType = tokenType;
        mValue = value;
        mLine = line;
        mFileName = fileName;
    }

    public TokenType getTokenType() {
        return mTokenType;
    }

    public String getValue() {
        return mValue;
    }

    public int getLine() {
        return mLine;
    }

    public String getFileName() {
        return mFileName;
    }

    public String toString() {
        return String.format("<%s , %s>", mTokenType, mValue);
    }

}
