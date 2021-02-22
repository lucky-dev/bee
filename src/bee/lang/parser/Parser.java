package bee.lang.parser;

/*

INT = [0-9][0-9]*
IDENTIFIER = [a-zA-Z][-_a-zA-Z0-9]*

EBNF grammar

Program -> { ClassDefinitionStatement }

ClassDefinitionStatement -> 'class' IDENTIFIER [ ':' IDENTIFIER ] '{'
    { AccessModifiers ( ConstructorDefinitionStatement | [ 'static' ] ( FieldDefinitionStatement | MethodDefinitionStatement ) ) }
'}'

FieldDefinitionStatement -> VariableDefinitionStatement

BaseVariableDeclaration -> ( 'var' | 'const' ) IDENTIFIER : ExtendedType

VariableDefinitionStatement -> BaseVariableDeclaration [ '=' ConditionalExpression ] ';'

ConstructorDefinitionStatement -> 'constructor' '(' [ FormalArgumentsList ] ')' [ ':' [ 'super' ] '(' [ ArgumentsList ] ')' ] '{' Statements '}'

MethodDefinitionStatement -> IDENTIFIER '(' [ FormalArgumentsList ] ')' [ ':' ExtendedType ] '{' Statements '}'

ArgumentsList -> ConditionalExpression { ',' ConditionalExpression }

FormalArgumentsList -> BaseVariableDeclaration { ',' BaseVariableDeclaration }

Statements -> { Statement }

Statement -> IfStatement | WhileStatement | DoWhileStatement | BlockStatement | VariableDefinitionStatement | BreakStatement | ContinueStatement | ReturnStatement | AssignmentStatement

IfStatement -> 'if' '(' ConditionalExpression ')' Statement [ 'else' Statement ]

WhileStatement -> 'while' '(' ConditionalExpression ')' Statement

DoWhileStatement -> 'do' Statement 'while' '(' ConditionalExpression ')' ';'

BlockStatement -> '{' Statements '}'

AssignmentStatement -> AssignmentExpression ';'

BreakStatement -> 'break' ';'

ContinueStatement -> 'continue' ';'

ReturnStatement -> 'return' [ ConditionalExpression ] ';'

AssignmentExpression -> UnaryExpression [ '=' ConditionalExpression ]

ConditionalExpression -> LogicalOrExpression [ '?' ConditionalExpression ':' ConditionalExpression ]

LogicalOrExpression -> LogicalAndExpression { '||' LogicalAndExpression }

LogicalAndExpression -> EqualityExpression { '&&' EqualityExpression }

EqualityExpression -> RelationalExpression { ( '==' | '!=' ) RelationalExpression }

RelationalExpression -> AddExpression { ( '<' | '>' | '<=' | '>=' ) AddExpression }

AddExpression -> MultExpression { ( '+' | '-' ) MultExpression }

MultExpression -> UnaryExpression { ( '*' | '/' | '%' ) UnaryExpression }

UnaryExpression -> ( '-' | '!' ) UnaryExpression | PostfixExpression

PostfixExpression -> PrimaryExpression { '[' ConditionalExpression ']' | '.' IDENTIFIER [ '(' [ ArgumentsList ] ')' ] }

PrimaryExpression -> 'this' | 'super' | 'true' | 'false' | 'nil' | IDENTIFIER [ '(' [ ArgumentsList ] ')' ] | INT_LITERAL | CHAR_LITERAL | STRING_LITERAL | '(' AssignmentExpression ')'
                    | 'new' ( Type '[' ConditionalExpression ']' { '[' ConditionalExpression ']' } | IDENTIFIER '(' [ ArgumentsList ] ')' )

AccessModifiers -> 'public' | 'protected' | 'private'

Type -> 'int' | 'char' | 'bool' | IDENTIFIER

ExtendedType -> Type { '[' ']' }

*/

import bee.lang.ast.*;
import bee.lang.ast.types.*;
import bee.lang.lexer.Lexer;
import bee.lang.lexer.Token;
import bee.lang.lexer.TokenType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

// Recursive descent parser

public class Parser {

    private Lexer mLexer;
    private Token mToken;
    private Token mPreviousToken;

    public Parser(Lexer lexer) {
        mLexer = lexer;
    }

    public Program parse(List<String> filePaths) {
        // This variable contains all statements of the program (all classes)
        Program program = new Program();

        for (String filePath : filePaths) {
            try {
                File file = new File(filePath);

                if ((file.exists()) && (file.isFile())) {
                    // Read each file
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

                    StringBuilder stringBuffer = new StringBuilder();

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line);
                        stringBuffer.append("\n");
                    }

                    // Reset state of the lexer to handle a new file
                    mLexer.initState(stringBuffer.toString(), file.getName());

                    mToken = mLexer.getNextToken();

                    // Parse the current file
                    Program newProgram = program();

                    // All new statements add to the list of all statements of the program
                    for (Statement statement : newProgram.getStatementsList()) {
                        program.addClassDefinition(statement);
                    }
                } else {
                    System.out.println("Not found source code in the file " + file.getName() + ".");
                }
            } catch (UnexpectedTokenException | IOException e) {
                e.printStackTrace();
            }
        }

        return program;
    }

    public Program parse(String text) {
        Program program = new Program();

        try {
            mLexer.initState(text, "");
            mToken = mLexer.getNextToken();
            program = program();
        } catch (UnexpectedTokenException e) {
            e.printStackTrace();
        }

        return program;
    }

    private Program program() throws UnexpectedTokenException {
        Program program = new Program();

        while (isCurrentToken(TokenType.CLASS)) {
            program.addClassDefinition(classDefinitionStatement());
        }

        match(TokenType.EOF);

        return program;
    }

    private Statement classDefinitionStatement() throws UnexpectedTokenException {
        match(TokenType.CLASS);

        match(TokenType.IDENTIFIER);

        Identifier classIdentifier = new Identifier(getPreviousToken());

        Identifier baseClassIdentifier = null;

        if (isCurrentToken(TokenType.COLON)) {
            match(TokenType.COLON);

            match(TokenType.IDENTIFIER);

            baseClassIdentifier = new Identifier(getPreviousToken());
        }

        match(TokenType.L_BRACE);

        Statements constructorDefinitions = new Statements();
        Statements methodDefinitions = new Statements();
        Statements fieldDefinitions = new Statements();

        while (isCurrentToken(TokenType.PUBLIC, TokenType.PROTECTED, TokenType.PRIVATE)) {
            AccessModifier accessModifier;

            if (isCurrentToken(TokenType.PROTECTED)) {
                accessModifier = AccessModifier.PROTECTED;
            } else if (isCurrentToken(TokenType.PRIVATE)) {
                accessModifier = AccessModifier.PRIVATE;
            } else {
                accessModifier = AccessModifier.PUBLIC;
            }

            match(TokenType.PUBLIC, TokenType.PROTECTED, TokenType.PRIVATE);

            if (isCurrentToken(TokenType.CONSTRUCTOR)) {
                constructorDefinitions.addStatement(constructorDefinitionStatement(accessModifier));
            } else {
                boolean isStatic = false;

                if (isCurrentToken(TokenType.STATIC)) {
                    match(TokenType.STATIC);
                    isStatic = true;
                }

                if (isCurrentToken(TokenType.VAR, TokenType.CONST)) {
                    fieldDefinitions.addStatement(fieldDefinitionStatement(accessModifier, isStatic));
                } else {
                    methodDefinitions.addStatement(methodDefinitionStatement(accessModifier, isStatic));
                }
            }
        }

        match(TokenType.R_BRACE);

        return new ClassDefinition(baseClassIdentifier, classIdentifier, constructorDefinitions, methodDefinitions, fieldDefinitions);
    }

    private Statement constructorDefinitionStatement(AccessModifier accessModifier) throws UnexpectedTokenException {
        Token constructorToken = mToken;

        match(TokenType.CONSTRUCTOR);

        Statements formalArgumentsList = new Statements();

        match(TokenType.L_PAREN);

        if (!isCurrentToken(TokenType.R_PAREN)) {
            formalArgumentsList = formalArgumentsList();
        }

        match(TokenType.R_PAREN);

        ArgumentsList superConstructorArgumentsList = new ArgumentsList();
        ArgumentsList otherConstructorArgumentsList = new ArgumentsList();

        if (isCurrentToken(TokenType.COLON)) {
            match(TokenType.COLON);

            boolean isSuperConstructor = false;

            if (isCurrentToken(TokenType.SUPER)) {
                match(TokenType.SUPER);
                isSuperConstructor = true;
            }

            match(TokenType.L_PAREN);

            if (!isCurrentToken(TokenType.R_PAREN)) {
                if (isSuperConstructor) {
                    superConstructorArgumentsList = argumentsList();
                } else {
                    otherConstructorArgumentsList = argumentsList();
                }
            }

            match(TokenType.R_PAREN);
        }

        match(TokenType.L_BRACE);

        Statements statements = new Statements();

        if (!isCurrentToken(TokenType.R_BRACE)) {
            statements = statements();
        }

        match(TokenType.R_BRACE);

        return new ConstructorDefinition(constructorToken, accessModifier, formalArgumentsList, superConstructorArgumentsList, otherConstructorArgumentsList, statements);
    }

    private Statement fieldDefinitionStatement(AccessModifier accessModifier, boolean isStatic) throws UnexpectedTokenException {
        return new FieldDefinition(getPreviousToken(), accessModifier, isStatic, variableDefinitionStatement());
    }

    private Statement methodDefinitionStatement(AccessModifier accessModifier, boolean isStatic) throws UnexpectedTokenException {
        match(TokenType.IDENTIFIER);

        Identifier identifier = new Identifier(getPreviousToken());

        Statements formalArgumentsList = new Statements();

        match(TokenType.L_PAREN);

        if (!isCurrentToken(TokenType.R_PAREN)) {
            formalArgumentsList = formalArgumentsList();
        }

        match(TokenType.R_PAREN);

        BaseType type;
        if (isCurrentToken(TokenType.COLON)) {
            match(TokenType.COLON);
            type = extendedType();
        } else {
            type = Type.Void;
        }

        match(TokenType.L_BRACE);

        Statements statements = new Statements();

        if (!isCurrentToken(TokenType.R_BRACE)) {
            statements = statements();
        }

        match(TokenType.R_BRACE);

        return new MethodDefinition(accessModifier, isStatic, identifier, formalArgumentsList, type, statements);
    }

    private VariableDefinition variableDefinitionStatement() throws UnexpectedTokenException {
        VariableDefinition variableDefinition = baseVariableDeclaration();

        Expression initExpression = null;

        if (isCurrentToken(TokenType.ASSIGN)) {
            match(TokenType.ASSIGN);
            initExpression = conditionalExpression();
        }

        match(TokenType.SEMICOLON);

        variableDefinition.setInitExpression(initExpression);

        return variableDefinition;
    }

    private VariableDefinition baseVariableDeclaration() throws UnexpectedTokenException {
        Token token = mToken;

        boolean isConst = false;

        if (isCurrentToken(TokenType.CONST)) {
            isConst = true;
            match(TokenType.CONST);
        } else {
            match(TokenType.VAR);
        }

        match(TokenType.IDENTIFIER);

        Identifier identifier = new Identifier(getPreviousToken());

        match(TokenType.COLON);

        BaseType type = extendedType();

        return new VariableDefinition(token, isConst, identifier, type, null);
    }

    private Statements statements() throws UnexpectedTokenException {
        Statements statements = new Statements();

        while (!isCurrentToken(TokenType.R_BRACE)) {
            statements.addStatement(statement());
        }

        return statements;
    }

    private Statement statement() throws UnexpectedTokenException {
        if (isCurrentToken(TokenType.IF)) {
            return ifStatement();
        } else if (isCurrentToken(TokenType.WHILE)) {
            return whileStatement();
        } else if (isCurrentToken(TokenType.DO)) {
            return doWhileStatement();
        } else if (isCurrentToken(TokenType.L_BRACE)) {
            return blockStatement();
        } else if (isCurrentToken(TokenType.VAR, TokenType.CONST)) {
            return variableDefinitionStatement();
        } else if (isCurrentToken(TokenType.BREAK)) {
            return breakStatement();
        } else if (isCurrentToken(TokenType.CONTINUE)) {
            return continueStatement();
        } else if (isCurrentToken(TokenType.RETURN)) {
            return returnStatement();
        } else {
            return assignmentStatement();
        }
    }

    private Statement ifStatement() throws UnexpectedTokenException {
        match(TokenType.IF);

        match(TokenType.L_PAREN);

        Expression expression = conditionalExpression();

        match(TokenType.R_PAREN);

        Statement thenStatement = statement();

        Statement elseStatement = null;
        if (isCurrentToken(TokenType.ELSE)) {
            match(TokenType.ELSE);
            elseStatement = statement();
        }

        return new If(expression, thenStatement, elseStatement);
    }

    private Statement whileStatement() throws UnexpectedTokenException {
        match(TokenType.WHILE);

        match(TokenType.L_PAREN);

        Expression expression = conditionalExpression();

        match(TokenType.R_PAREN);

        return new While(expression, statement());
    }

    private Statement doWhileStatement() throws UnexpectedTokenException {
        match(TokenType.DO);

        Statement statement = statement();

        match(TokenType.WHILE);

        match(TokenType.L_PAREN);

        Expression expression = conditionalExpression();

        match(TokenType.R_PAREN);

        match(TokenType.SEMICOLON);

        return new DoWhile(expression, statement);
    }

    private Statement blockStatement() throws UnexpectedTokenException {
        match(TokenType.L_BRACE);

        Statements statement = statements();

        match(TokenType.R_BRACE);

        return new Block(statement);
    }

    private Statement assignmentStatement() throws UnexpectedTokenException {
        AssignmentStatement assignmentStatement = new AssignmentStatement(assignmentExpression());

        match(TokenType.SEMICOLON);

        return assignmentStatement;
    }

    private Statement breakStatement() throws UnexpectedTokenException {
        match(TokenType.BREAK);

        Break breakStatement = new Break();

        match(TokenType.SEMICOLON);

        return breakStatement;
    }

    private Statement continueStatement() throws UnexpectedTokenException {
        match(TokenType.CONTINUE);

        Continue continueStatement = new Continue();

        match(TokenType.SEMICOLON);

        return continueStatement;
    }

    private Statement returnStatement() throws UnexpectedTokenException {
        match(TokenType.RETURN);

        if (!isCurrentToken(TokenType.SEMICOLON)) {
            Return returnStatement = new Return(conditionalExpression());
            match(TokenType.SEMICOLON);
            return returnStatement;
        }

        return new Return();
    }

    private ArgumentsList argumentsList() throws UnexpectedTokenException {
        ArgumentsList argumentsList = new ArgumentsList();
        argumentsList.addExpression(conditionalExpression());

        while (isCurrentToken(TokenType.COMMA)) {
            match(TokenType.COMMA);
            argumentsList.addExpression(conditionalExpression());
        }

        return argumentsList;
    }

    private Statements formalArgumentsList() throws UnexpectedTokenException {
        Statements argumentsList = new Statements();
        argumentsList.addStatement(baseVariableDeclaration());

        while (isCurrentToken(TokenType.COMMA)) {
            match(TokenType.COMMA);
            argumentsList.addStatement(baseVariableDeclaration());
        }

        return argumentsList;
    }

    private Expression assignmentExpression() throws UnexpectedTokenException {
        Expression expression = unaryExpression();

        if (isCurrentToken(TokenType.ASSIGN)) {
            match(TokenType.ASSIGN);
            expression = new Assignment(getPreviousToken(), expression, conditionalExpression());
        }

        return expression;
    }

    private Expression conditionalExpression() throws UnexpectedTokenException {
        Expression expression = logicalOrExpression();

        if (isCurrentToken(TokenType.QUESTION_MARK)) {
            match(TokenType.QUESTION_MARK);
            Expression thenExpression = conditionalExpression();
            match(TokenType.COLON);
            expression = new TernaryOperator(getPreviousToken(), expression, thenExpression, conditionalExpression());
        }

        return expression;
    }

    private Expression logicalOrExpression() throws UnexpectedTokenException {
        Expression expression = logicalAndExpression();

        while (isCurrentToken(TokenType.OR)) {
            match(TokenType.OR);
            expression = new Or(getPreviousToken(), expression, logicalAndExpression());
        }

        return expression;
    }

    private Expression logicalAndExpression() throws UnexpectedTokenException {
        Expression expression = equalityExpression();

        while (isCurrentToken(TokenType.AND)) {
            match(TokenType.AND);
            expression = new And(getPreviousToken(), expression, equalityExpression());
        }

        return expression;
    }

    private Expression equalityExpression() throws UnexpectedTokenException {
        Expression expression = relationalExpression();

        while (isCurrentToken(TokenType.EQ, TokenType.NOT_EQ)) {
            if (isCurrentToken(TokenType.EQ)) {
                match(TokenType.EQ);
                expression = new Equal(getPreviousToken(), expression, relationalExpression());
            } else {
                match(TokenType.NOT_EQ);
                expression = new NotEqual(getPreviousToken(), expression, relationalExpression());
            }
        }

        return expression;
    }

    private Expression relationalExpression() throws UnexpectedTokenException {
        Expression expression = addExpression();

        while (isCurrentToken(TokenType.LT, TokenType.LE, TokenType.GT, TokenType.GE)) {
            if (isCurrentToken(TokenType.LT)) {
                match(TokenType.LT);
                expression = new LessThan(getPreviousToken(), expression, addExpression());
            } else if (isCurrentToken(TokenType.LE)) {
                match(TokenType.LE);
                expression = new LessEqualThan(getPreviousToken(), expression, addExpression());
            } else if (isCurrentToken(TokenType.GT)) {
                match(TokenType.GT);
                expression = new GreaterThan(getPreviousToken(), expression, addExpression());
            } else {
                match(TokenType.GE);
                expression = new GreaterEqualThan(getPreviousToken(), expression, addExpression());
            }
        }

        return expression;
    }

    private Expression addExpression() throws UnexpectedTokenException {
        Expression expression = multExpression();

        while (isCurrentToken(TokenType.PLUS, TokenType.MINUS)) {
            if (isCurrentToken(TokenType.PLUS)) {
                match(TokenType.PLUS);
                expression = new Add(getPreviousToken(), expression, multExpression());
            } else {
                match(TokenType.MINUS);
                expression = new Subtract(getPreviousToken(), expression, multExpression());
            }
        }

        return expression;
    }

    private Expression multExpression() throws UnexpectedTokenException {
        Expression expression = unaryExpression();

        while (isCurrentToken(TokenType.TIMES, TokenType.DIV, TokenType.MOD)) {
            if (isCurrentToken(TokenType.TIMES)) {
                match(TokenType.TIMES);
                expression = new Times(getPreviousToken(), expression, unaryExpression());
            } else if (isCurrentToken(TokenType.DIV)) {
                match(TokenType.DIV);
                expression = new Div(getPreviousToken(), expression, unaryExpression());
            } else if (isCurrentToken(TokenType.MOD)) {
                match(TokenType.MOD);
                expression = new Mod(getPreviousToken(), expression, unaryExpression());
            }
        }

        return expression;
    }

    private Expression unaryExpression() throws UnexpectedTokenException {
        if (isCurrentToken(TokenType.MINUS)) {
            match(TokenType.MINUS);
            return new UnaryMinus(getPreviousToken(), unaryExpression());
        } else if (isCurrentToken(TokenType.NOT)) {
            match(TokenType.NOT);
            return new Not(getPreviousToken(), unaryExpression());
        } else {
            return postfixExpression();
        }
    }

    private Expression postfixExpression() throws UnexpectedTokenException {
        Expression expression = primaryExpression();

        while (isCurrentToken(TokenType.L_SQ_PAREN, TokenType.DOT)) {
            if (isCurrentToken(TokenType.L_SQ_PAREN)) {
                match(TokenType.L_SQ_PAREN);
                expression = new ArrayAccess(getPreviousToken(), expression, conditionalExpression());
                match(TokenType.R_SQ_PAREN);
            } else {
                match(TokenType.DOT);

                match(TokenType.IDENTIFIER);

                Identifier identifier = new Identifier(getPreviousToken());

                if (isCurrentToken(TokenType.L_PAREN)) {
                    match(TokenType.L_PAREN);

                    ArgumentsList argumentsList = new ArgumentsList();

                    if (!isCurrentToken(TokenType.R_PAREN)) {
                        argumentsList = argumentsList();
                    }

                    match(TokenType.R_PAREN);

                    expression = new Call(expression, identifier, argumentsList);
                } else {
                    expression = new FieldAccess(expression, identifier);
                }
            }
        }

        return expression;
    }

    private Expression primaryExpression() throws UnexpectedTokenException {
        if (isCurrentToken(TokenType.THIS)) {
            match(TokenType.THIS);
            return new This(getPreviousToken());
        } else if (isCurrentToken(TokenType.SUPER)) {
            match(TokenType.SUPER);
            return new Super(getPreviousToken());
        } else if (isCurrentToken(TokenType.TRUE)) {
            match(TokenType.TRUE);
            return new BoolLiteral(true);
        } else if (isCurrentToken(TokenType.FALSE)) {
            match(TokenType.FALSE);
            return new BoolLiteral(false);
        } else if (isCurrentToken(TokenType.NIL)) {
            match(TokenType.NIL);
            return new Nil();
        } else if (isCurrentToken(TokenType.IDENTIFIER)) {
            Identifier identifier = new Identifier(mToken);
            match(TokenType.IDENTIFIER);

            if (isCurrentToken(TokenType.L_PAREN)) {
                match(TokenType.L_PAREN);

                ArgumentsList argumentsList = new ArgumentsList();

                if (!isCurrentToken(TokenType.R_PAREN)) {
                    argumentsList = argumentsList();
                }

                match(TokenType.R_PAREN);

                return new Call(new This(mToken), identifier, argumentsList);
            } else {
                return identifier;
            }
        } else if (isCurrentToken(TokenType.INT_LITERAL)) {
            IntLiteral intLiteral = new IntLiteral(mToken);
            match(TokenType.INT_LITERAL);
            return intLiteral;
        } else if (isCurrentToken(TokenType.CHAR_LITERAL)) {
            CharLiteral charLiteral = new CharLiteral(mToken);
            match(TokenType.CHAR_LITERAL);
            return charLiteral;
        } else if (isCurrentToken(TokenType.STRING_LITERAL)) {
            StringLiteral stringLiteral = new StringLiteral(mToken);
            match(TokenType.STRING_LITERAL);
            return stringLiteral;
        } else if (isCurrentToken(TokenType.L_PAREN)) {
            match(TokenType.L_PAREN);
            Expression expression = assignmentExpression();
            match(TokenType.R_PAREN);
            return expression;
        } else {
            Token token = mToken;

            match(TokenType.NEW);

            BaseType type = type();

            if (isCurrentToken(TokenType.L_SQ_PAREN)) {
                match(TokenType.L_SQ_PAREN);
                ArrayType arrayType = new ArrayType(type);
                NewArray newArray = new NewArray(type, conditionalExpression(), null);
                match(TokenType.R_SQ_PAREN);

                while (isCurrentToken(TokenType.L_SQ_PAREN)) {
                    match(TokenType.L_SQ_PAREN);
                    arrayType = new ArrayType(arrayType);
                    newArray = new NewArray(arrayType, conditionalExpression(), newArray);
                    match(TokenType.R_SQ_PAREN);
                }

                return newArray;
            } else {
                match(TokenType.L_PAREN);

                ArgumentsList argumentsList = new ArgumentsList();
                if (!isCurrentToken(TokenType.R_PAREN)) {
                    argumentsList = argumentsList();
                }

                match(TokenType.R_PAREN);

                return new NewObject(token, type, argumentsList);
            }
        }
    }

    private BaseType extendedType() throws UnexpectedTokenException {
        BaseType type = type();

        if (isCurrentToken(TokenType.L_SQ_PAREN)) {
            match(TokenType.L_SQ_PAREN);
            ArrayType arrayType = new ArrayType(type);
            match(TokenType.R_SQ_PAREN);

            while (isCurrentToken(TokenType.L_SQ_PAREN)) {
                match(TokenType.L_SQ_PAREN);
                arrayType = new ArrayType(arrayType);
                match(TokenType.R_SQ_PAREN);
            }

            type = arrayType;
        }

        return type;
    }

    private BaseType type() throws UnexpectedTokenException {
        BaseType type;
        if (isCurrentToken(TokenType.INT_TYPE)) {
            match(TokenType.INT_TYPE);
            type = Type.Int;
        } else if (isCurrentToken(TokenType.CHAR_TYPE)) {
            match(TokenType.CHAR_TYPE);
            type = Type.Char;
        } else if (isCurrentToken(TokenType.BOOL_TYPE)) {
            match(TokenType.BOOL_TYPE);
            type = Type.Bool;
        } else {
            match(TokenType.IDENTIFIER);
            type = Type.defineClassType(new Identifier(getPreviousToken()));
        }

        return type;
    }

    private void match(TokenType expectedToken) throws UnexpectedTokenException {
        if (isCurrentToken(expectedToken)) {
            mPreviousToken = mToken;
            mToken = mLexer.getNextToken();
            return;
        }

        throw new UnexpectedTokenException(mToken);
    }

    private void match(TokenType... expectedToken) throws UnexpectedTokenException {
        for (TokenType tokenType : expectedToken) {
            if (isCurrentToken(tokenType)) {
                mPreviousToken = mToken;
                mToken = mLexer.getNextToken();
                return;
            }
        }

        throw new UnexpectedTokenException(mToken);
    }

    private boolean isCurrentToken(TokenType... expectedToken) {
        for (TokenType tokenType : expectedToken) {
            if (isCurrentToken(tokenType)) {
                return true;
            }
        }

        return false;
    }

    private boolean isCurrentToken(TokenType tokenType) {
        return mToken.getTokenType() == tokenType;
    }

    private Token getPreviousToken() {
        return mPreviousToken;
    }

    private static class UnexpectedTokenException extends Exception {

        public UnexpectedTokenException(Token token) {
            super("Unexpected token " + token.getTokenType() + ".");
        }

    }

}
