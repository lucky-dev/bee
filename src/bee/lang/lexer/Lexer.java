package bee.lang.lexer;

import java.util.HashMap;

public class Lexer {

    private static int MAX_LENGTH_ID = 30;
    private static int MAX_LENGTH_STR_LITERAL = 255;

    private String mText;
    private String mFileName;
    private int mNumberOfLine;
    private int mCurrentPosition;
    private char mCurrentChar;
    private HashMap<String, Token> mReservedTokens;
    private Token mEofToken;

    public Lexer() {
        initState("", "");

        mEofToken = createToken(TokenType.EOF);

        mReservedTokens = new HashMap<>();
        mReservedTokens.put("if", createToken(TokenType.IF));
        mReservedTokens.put("else", createToken(TokenType.ELSE));
        mReservedTokens.put("while", createToken(TokenType.WHILE));
        mReservedTokens.put("for", createToken(TokenType.FOR));
        mReservedTokens.put("do", createToken(TokenType.DO));
        mReservedTokens.put("break", createToken(TokenType.BREAK));
        mReservedTokens.put("continue", createToken(TokenType.CONTINUE));
        mReservedTokens.put("class", createToken(TokenType.CLASS));
        mReservedTokens.put("public", createToken(TokenType.PUBLIC));
        mReservedTokens.put("protected", createToken(TokenType.PROTECTED));
        mReservedTokens.put("private", createToken(TokenType.PRIVATE));
        mReservedTokens.put("true", createToken(TokenType.TRUE));
        mReservedTokens.put("false", createToken(TokenType.FALSE));
        mReservedTokens.put("nil", createToken(TokenType.NIL));
        mReservedTokens.put("new", createToken(TokenType.NEW));
        mReservedTokens.put("static", createToken(TokenType.STATIC));
        mReservedTokens.put("return", createToken(TokenType.RETURN));
        mReservedTokens.put("int", createToken(TokenType.INT_TYPE));
//        mReservedTokens.put("float", createToken(TokenType.FLOAT_TYPE));
        mReservedTokens.put("bool", createToken(TokenType.BOOL_TYPE));
        mReservedTokens.put("char", createToken(TokenType.CHAR_TYPE));
//        mReservedTokens.put("require", createToken(TokenType.REQUIRE));
        mReservedTokens.put("const", createToken(TokenType.CONST));
        mReservedTokens.put("var", createToken(TokenType.VAR));
        mReservedTokens.put("super", createToken(TokenType.SUPER));
        mReservedTokens.put("this", createToken(TokenType.THIS));
        mReservedTokens.put("constructor", createToken(TokenType.CONSTRUCTOR));
    }

    public void initState(String text, String fileName) {
        mText = text;
        mNumberOfLine = 0;
        mCurrentPosition = -1;
        mCurrentChar = ' ';
        mFileName = fileName;
    }

    public Token getNextToken() {
        moveToNextChar();

        while (!isEnd()) {
            skipSpaces();

            if (isEnd()) {
                return mEofToken;
            } else if (isCurrentChar('/')) { // Find comments
                moveToNextChar();

                if (isCurrentChar('*')) {
                    boolean isInsideOfComment = true;

                    while (moveToNextChar()) {
                        if (isCurrentChar('*')) {
                            moveToNextChar();

                            if (isCurrentChar('/')) {
                                isInsideOfComment = false;
                                break;
                            }
                        }
                    }

                    if (isInsideOfComment) {
                        System.out.println("Lexer does not see the end of the multiline comment.");

                        return mEofToken;
                    }
                } else if (isCurrentChar('/')) {
                    while (moveToNextChar()) {
                        if (isCurrentChar('\n')) {
                            break;
                        }
                    }

                    if (isEnd()) {
                        break;
                    }
                } else {
                    return createToken(TokenType.DIV);
                }
            } else if (isCurrentCharLetter()) { // Find identifiers
                StringBuilder lexeme = new StringBuilder();
                lexeme.append(mCurrentChar);

                moveToNextChar();

                while (isCurrentCharLetterOrDigit()) {
                    lexeme.append(mCurrentChar);
                    moveToNextChar();
                }

                moveToPreviousToken();

                String sLexeme = lexeme.toString();
                if (mReservedTokens.containsKey(sLexeme)) {
                    return mReservedTokens.get(sLexeme);
                } else {
                    if (sLexeme.length() <= MAX_LENGTH_ID) {
                        Token token = createToken(TokenType.IDENTIFIER, sLexeme);
                        mReservedTokens.put(sLexeme, token);
                        return token;
                    } else {
                        System.out.println("Too long identifier (max size " + MAX_LENGTH_ID + " symbols) in the line " + mNumberOfLine + ".");

                        moveToEndOfLine();
                    }
                }
            } else if (isCurrentCharDigit()) { // Find INT and FLOAT (the current version does not support FLOAT)
                StringBuilder lexeme = new StringBuilder();
                lexeme.append(mCurrentChar);

                moveToNextChar();

                while (isCurrentCharDigit()) {
                    lexeme.append(mCurrentChar);
                    moveToNextChar();
                }

                /*if (isCurrentChar('.')) {
                    lexeme.append(mCurrentChar);

                    moveToNextChar();

                    if (isCurrentCharDigit()) {
                        while (isCurrentCharDigit()) {
                            lexeme.append(mCurrentChar);
                            moveToNextChar();
                        }

                        moveToPreviousToken();

                        return createToken(TokenType.FLOAT_LITERAL, lexeme.toString());
                    } else {
                        if (isEnd()) {
                            System.out.println("An unexpected the end of the file.");
                        } else {
                            System.out.println("An unexpected character '" + mCurrentChar + "' in the line " + mNumberOfLines + ".");

                            moveToEndOfLine();
                        }
                    }
                } else {*/
                    moveToPreviousToken();

                    return createToken(TokenType.INT_LITERAL, lexeme.toString());
                /*}*/
            } else if (isCurrentChar('(')) {
                return createToken(TokenType.L_PAREN);
            } else if (isCurrentChar(')')) {
                return createToken(TokenType.R_PAREN);
            } else if (isCurrentChar('[')) {
                return createToken(TokenType.L_SQ_PAREN);
            } else if (isCurrentChar(']')) {
                return createToken(TokenType.R_SQ_PAREN);
            } else if (isCurrentChar('{')) {
                return createToken(TokenType.L_BRACE);
            } else if (isCurrentChar('}')) {
                return createToken(TokenType.R_BRACE);
            } else if (isCurrentChar(';')) {
                return createToken(TokenType.SEMICOLON);
            } else if (isCurrentChar('.')) {
                return createToken(TokenType.DOT);
            } else if (isCurrentChar(',')) {
                return createToken(TokenType.COMMA);
            } else if (isCurrentChar('=')) {
                moveToNextChar();

                if (isCurrentChar('=')) {
                    return createToken(TokenType.EQ);
                } else {
                    return createToken(TokenType.ASSIGN);
                }
            } else if (isCurrentChar('<')) {
                moveToNextChar();

                if (isCurrentChar('=')) {
                    return createToken(TokenType.LE);
                } else {
                    return createToken(TokenType.LT);
                }
            } else if (isCurrentChar('>')) {
                moveToNextChar();

                if (isCurrentChar('=')) {
                    return createToken(TokenType.GE);
                } else {
                    return createToken(TokenType.GT);
                }
            } else if (isCurrentChar('!')) {
                moveToNextChar();

                if (isCurrentChar('=')) {
                    return createToken(TokenType.NOT_EQ);
                } else {
                    return createToken(TokenType.NOT);
                }
            } else if (isCurrentChar('&')) {
                moveToNextChar();

                if (isCurrentChar('&')) {
                    return createToken(TokenType.AND);
                }
            } else if (isCurrentChar('|')) {
                if (isCurrentChar('|')) {
                    return createToken(TokenType.OR);
                }
            } else if (isCurrentChar('+')) {
                return createToken(TokenType.PLUS);
            } else if (isCurrentChar('-')) {
                return createToken(TokenType.MINUS);
            } else if (isCurrentChar('*')) {
                return createToken(TokenType.TIMES);
            } else if (isCurrentChar('?')) {
                return createToken(TokenType.QUESTION_MARK);
            } else if (isCurrentChar(':')) {
                return createToken(TokenType.COLON);
            } else if (isCurrentChar('%')) {
                return createToken(TokenType.MOD);
            } else if (isCurrentChar('"')) { // Find strings
                boolean isInsideOfString = true;

                StringBuilder str = new StringBuilder();

                while (moveToNextChar()) {
                    if (isCurrentChar('"')) {
                        isInsideOfString = false;
                        break;
                    }

                    if (isCurrentChar('\n')) {
                        break;
                    }

                    str.append(mCurrentChar);
                }

                if (isInsideOfString) {
                    System.out.println("Lexer does not see \" at the end of the string in the line " + mNumberOfLine + ".");

                    moveToEndOfLine();
                } else if (str.length() > MAX_LENGTH_STR_LITERAL) {
                    System.out.println("Too long string literal (max size " + MAX_LENGTH_STR_LITERAL + " symbols) in the line " + mNumberOfLine + ".");

                    moveToEndOfLine();
                } else {
                    return createToken(TokenType.STRING_LITERAL, str.toString());
                }
            } else if (isCurrentChar('\'')) { // Find chars
                StringBuilder ch = new StringBuilder();

                moveToNextChar();

                if (isCurrentChar('\\')) {
                    ch.append(mCurrentChar);

                    moveToNextChar();

                    ch.append(mCurrentChar);

                    String newChar = ch.toString();

                    if ((newChar.equals("\\'")) ||
                            (newChar.equals("\\\"")) ||
                            (newChar.equals("\\\\")) ||
                            (newChar.equals("\\n")) ||
                            (newChar.equals("\\r")) ||
                            (newChar.equals("\\t")) ||
                            (newChar.equals("\\b")) ||
                            (newChar.equals("\\0"))) {
                        moveToNextChar();

                        if (isCurrentChar('\'')) {
                            return createToken(TokenType.CHAR_LITERAL, ch.toString());
                        }
                    }
                } else {
                    ch.append(mCurrentChar);

                    moveToNextChar();

                    if (isCurrentChar('\'')) {
                        return createToken(TokenType.CHAR_LITERAL, ch.toString());
                    }
                }

                if (isEnd()) {
                    System.out.println("An unexpected the end of the file.");
                } else {
                    System.out.println("An unexpected character '" + mCurrentChar + "' in the line " + mNumberOfLine + ".");

                    moveToEndOfLine();
                }
            } else {
                System.out.println("An unexpected character '" + mCurrentChar + "' in the line " + mNumberOfLine + ".");

                moveToEndOfLine();
            }

            moveToNextChar();
        }

        return mEofToken;
    }

    private Token createToken(TokenType tokenType) {
        return createToken(tokenType, null);
    }

    private Token createToken(TokenType tokenType, String value) {
        return new Token(tokenType, value, mNumberOfLine, mFileName);
    }

    private boolean moveToNextChar() {
        if (isEnd()) {
            return false;
        }

        mCurrentPosition++;

        if (isEnd()) {
            mCurrentChar = ' ';
            return false;
        }

        mCurrentChar = mText.charAt(mCurrentPosition);

        return true;
    }

    private void moveToPreviousToken() {
        if (mCurrentPosition == 0) {
            return;
        }

        mCurrentPosition--;
        mCurrentChar = mText.charAt(mCurrentPosition);
    }

    private boolean isEnd() {
        return mCurrentPosition == mText.length();
    }

    private boolean isCurrentChar(char ch) {
        return mCurrentChar == ch;
    }

    private void skipSpaces() {
        if (isEnd()) {
            return;
        }

        while ((isCurrentChar('\t')) ||
                (isCurrentChar(' ')) ||
                (isCurrentChar('\r')) ||
                (isCurrentChar('\n'))) {

            if (isCurrentChar('\n')) {
                mNumberOfLine++;
            }

            if (!moveToNextChar()) {
                break;
            }
        }
    }

    private void moveToEndOfLine() {
        if (isEnd()) {
            return;
        }

        while (!isCurrentChar('\n')) {
            if (!moveToNextChar()) {
                break;
            }
        }
    }

    private boolean isCurrentCharLetter() {
        return Character.isLetter(mCurrentChar);
    }

    private boolean isCurrentCharDigit() {
        return Character.isDigit(mCurrentChar);
    }

    private boolean isCurrentCharLetterOrDigit() {
        return isCurrentCharLetter() || isCurrentCharDigit();
    }

}
