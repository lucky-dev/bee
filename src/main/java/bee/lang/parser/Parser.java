package bee.lang.parser;

/*

INT = [0-9][0-9]*
IDENTIFIER = [a-zA-Z][-_a-zA-Z0-9]*

EBNF grammar

Program -> { ClassDefinitionStatement }

ClassDefinitionStatement -> 'class' IDENTIFIER [ ':' IDENTIFIER ] '{'
    { ( ( AccessModifiers ConstructorDefinitionStatement ) | ( ( ( [AccessModifiers] [ 'static' ] ) | ( [ 'static' ] [AccessModifiers] ) ) ( FieldDefinitionStatement | MethodDefinitionStatement ) ) | ExternalFunctionDefinitionStatement ) }
'}'

FieldDefinitionStatement -> VariableDefinitionStatement

BaseVariableDeclaration -> ( 'var' | 'const' ) IDENTIFIER : ExtendedType

VariableDefinitionStatement -> BaseVariableDeclaration [ '=' ConditionalExpression ] ';'

ConstructorDefinitionStatement -> 'constructor' '(' [ FormalArgumentsList ] ')' [ ':' [ 'super' ] '(' [ ArgumentsList ] ')' ] '{' Statements '}'

MethodDefinitionStatement -> IDENTIFIER '(' [ FormalArgumentsList ] ')' [ ':' ExtendedType ] '{' Statements '}'

ExternalFunctionDefinitionStatement -> 'external' IDENTIFIER '(' [ FormalArgumentsList ] ')' [ ':' ExtendedType ] ';'

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
                    | 'new' ( Type '[' ConditionalExpression ']' { '[' ConditionalExpression ']' } | IDENTIFIER '(' [ ArgumentsList ] ')' ) | '@' IDENTIFIER  '(' [ ArgumentsList ] ')'

AccessModifiers -> 'public' | 'protected' | 'private'

Type -> 'int' | 'char' | 'bool' | IDENTIFIER

ExtendedType -> Type { '[' ']' }

*/

import bee.lang.ast.*;
import bee.lang.ast.types.*;
import bee.lang.exceptions.ArrayDimensionException;
import bee.lang.exceptions.BaseParserException;
import bee.lang.exceptions.UnexpectedTokenException;
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

    public Program parse(List<String> filePaths) throws BaseParserException {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return program;
    }

    public Program parse(String text) throws BaseParserException {
        mLexer.initState(text, "");
        mToken = mLexer.getNextToken();

        return program();
    }

    private Program program() throws BaseParserException {
        Program program = new Program();

        while (isCurrentToken(TokenType.CLASS)) {
            program.addClassDefinition(classDefinitionStatement());
        }

        match(TokenType.EOF);

        return program;
    }

    private AccessModifier findAccessModifier() throws BaseParserException {
        AccessModifier accessModifier = null;

        if (isCurrentToken(TokenType.PROTECTED)) {
            accessModifier = AccessModifier.PROTECTED;
            match(TokenType.PROTECTED);
        } else if (isCurrentToken(TokenType.PRIVATE)) {
            accessModifier = AccessModifier.PRIVATE;
            match(TokenType.PRIVATE);
        } else if (isCurrentToken(TokenType.PUBLIC)) {
            match(TokenType.PUBLIC);
            accessModifier = AccessModifier.PUBLIC;
        }

        return accessModifier;
    }

    private Statement classDefinitionStatement() throws BaseParserException {
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
        Statements externalFunctionDeclarations = new Statements();

        while (!isCurrentToken(TokenType.R_BRACE)) {
            if (isCurrentToken(TokenType.EXTERNAL)) {
                externalFunctionDeclarations.addStatement(externalFunctionDeclarationStatement());
            } else {
                AccessModifier accessModifier = findAccessModifier();

                if (isCurrentToken(TokenType.CONSTRUCTOR)) {
                    if (accessModifier == null) {
                        accessModifier = AccessModifier.PUBLIC;
                    }

                    constructorDefinitions.addStatement(constructorDefinitionStatement(accessModifier));
                } else {
                    boolean isStatic = false;

                    if (isCurrentToken(TokenType.STATIC)) {
                        match(TokenType.STATIC);
                        isStatic = true;

                        if (accessModifier == null) {
                            accessModifier = findAccessModifier();
                        }
                    }

                    if (isCurrentToken(TokenType.VAR, TokenType.CONST)) {
                        if (accessModifier == null) {
                            accessModifier = AccessModifier.PRIVATE;
                        }

                        fieldDefinitions.addStatement(fieldDefinitionStatement(accessModifier, isStatic));
                    } else {
                        if (accessModifier == null) {
                            accessModifier = AccessModifier.PUBLIC;
                        }

                        methodDefinitions.addStatement(methodDefinitionStatement(accessModifier, isStatic));
                    }
                }
            }
        }

        match(TokenType.R_BRACE);

        return new ClassDefinition(baseClassIdentifier, classIdentifier, constructorDefinitions, methodDefinitions, fieldDefinitions, externalFunctionDeclarations);
    }

    private Statement constructorDefinitionStatement(AccessModifier accessModifier) throws BaseParserException {
        Token constructorToken = mToken;

        match(TokenType.CONSTRUCTOR);

        Statements formalArgumentsList = new Statements();

        match(TokenType.L_PAREN);

        if (!isCurrentToken(TokenType.R_PAREN)) {
            formalArgumentsList = formalArgumentsList();
        }

        match(TokenType.R_PAREN);

        ArgumentsList superConstructorArgumentsList = null;
        ArgumentsList otherConstructorArgumentsList = null;

        if (isCurrentToken(TokenType.COLON)) {
            match(TokenType.COLON);

            boolean isSuperConstructor = false;

            if (isCurrentToken(TokenType.SUPER)) {
                match(TokenType.SUPER);
                isSuperConstructor = true;
                superConstructorArgumentsList = new ArgumentsList();
            } else {
                otherConstructorArgumentsList = new ArgumentsList();
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

    private Statement fieldDefinitionStatement(AccessModifier accessModifier, boolean isStatic) throws BaseParserException {
        return new FieldDefinition(getPreviousToken(), accessModifier, isStatic, variableDefinitionStatement());
    }

    private Statement methodDefinitionStatement(AccessModifier accessModifier, boolean isStatic) throws BaseParserException {
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

    private Statement externalFunctionDeclarationStatement() throws BaseParserException {
        match(TokenType.EXTERNAL);

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

        match(TokenType.SEMICOLON);

        return new ExternalFunctionDeclaration(identifier, formalArgumentsList, type);
    }

    private VariableDefinition variableDefinitionStatement() throws BaseParserException {
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

    private VariableDefinition baseVariableDeclaration() throws BaseParserException {
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

    private Statements statements() throws BaseParserException {
        Statements statements = new Statements();

        while (!isCurrentToken(TokenType.R_BRACE)) {
            statements.addStatement(statement());
        }

        return statements;
    }

    private Statement statement() throws BaseParserException {
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

    private Statement ifStatement() throws BaseParserException {
        Token token = mToken;

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

        return new If(expression, thenStatement, elseStatement, token);
    }

    private Statement whileStatement() throws BaseParserException {
        Token token = mToken;

        match(TokenType.WHILE);

        match(TokenType.L_PAREN);

        Expression expression = conditionalExpression();

        match(TokenType.R_PAREN);

        return new While(expression, statement(), token);
    }

    private Statement doWhileStatement() throws BaseParserException {
        match(TokenType.DO);

        Statement statement = statement();

        Token token = mToken;

        match(TokenType.WHILE);

        match(TokenType.L_PAREN);

        Expression expression = conditionalExpression();

        match(TokenType.R_PAREN);

        match(TokenType.SEMICOLON);

        return new DoWhile(expression, statement, token);
    }

    private Statement blockStatement() throws BaseParserException {
        match(TokenType.L_BRACE);

        Statements statement = statements();

        match(TokenType.R_BRACE);

        return new Block(statement);
    }

    private Statement assignmentStatement() throws BaseParserException {
        AssignmentStatement assignmentStatement = new AssignmentStatement(assignmentExpression());

        match(TokenType.SEMICOLON);

        return assignmentStatement;
    }

    private Statement breakStatement() throws BaseParserException {
        match(TokenType.BREAK);

        Break breakStatement = new Break(getPreviousToken());

        match(TokenType.SEMICOLON);

        return breakStatement;
    }

    private Statement continueStatement() throws BaseParserException {
        match(TokenType.CONTINUE);

        Continue continueStatement = new Continue(getPreviousToken());

        match(TokenType.SEMICOLON);

        return continueStatement;
    }

    private Statement returnStatement() throws BaseParserException {
        Token token = mToken;

        match(TokenType.RETURN);

        if (!isCurrentToken(TokenType.SEMICOLON)) {
            Return returnStatement = new Return(token, conditionalExpression());
            match(TokenType.SEMICOLON);
            return returnStatement;
        } else {
            match(TokenType.SEMICOLON);
            return new Return(token);
        }
    }

    private ArgumentsList argumentsList() throws BaseParserException {
        ArgumentsList argumentsList = new ArgumentsList();
        argumentsList.addExpression(conditionalExpression());

        while (isCurrentToken(TokenType.COMMA)) {
            match(TokenType.COMMA);
            argumentsList.addExpression(conditionalExpression());
        }

        return argumentsList;
    }

    private Statements formalArgumentsList() throws BaseParserException {
        Statements argumentsList = new Statements();
        argumentsList.addStatement(baseVariableDeclaration());

        while (isCurrentToken(TokenType.COMMA)) {
            match(TokenType.COMMA);
            argumentsList.addStatement(baseVariableDeclaration());
        }

        return argumentsList;
    }

    private Expression assignmentExpression() throws BaseParserException {
        Expression expression = unaryExpression();

        if (isCurrentToken(TokenType.ASSIGN)) {
            match(TokenType.ASSIGN);
            expression = new Assignment(getPreviousToken(), expression, conditionalExpression());
        }

        return expression;
    }

    private Expression conditionalExpression() throws BaseParserException {
        Expression expression = logicalOrExpression();

        if (isCurrentToken(TokenType.QUESTION_MARK)) {
            match(TokenType.QUESTION_MARK);
            Expression thenExpression = conditionalExpression();
            match(TokenType.COLON);
            expression = new TernaryOperator(getPreviousToken(), expression, thenExpression, conditionalExpression());
        }

        return expression;
    }

    private Expression logicalOrExpression() throws BaseParserException {
        Expression expression = logicalAndExpression();

        while (isCurrentToken(TokenType.OR)) {
            match(TokenType.OR);
            expression = new Or(getPreviousToken(), expression, logicalAndExpression());
        }

        return expression;
    }

    private Expression logicalAndExpression() throws BaseParserException {
        Expression expression = equalityExpression();

        while (isCurrentToken(TokenType.AND)) {
            match(TokenType.AND);
            expression = new And(getPreviousToken(), expression, equalityExpression());
        }

        return expression;
    }

    private Expression equalityExpression() throws BaseParserException {
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

    private Expression relationalExpression() throws BaseParserException {
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

    private Expression addExpression() throws BaseParserException {
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

    private Expression multExpression() throws BaseParserException {
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

    private Expression unaryExpression() throws BaseParserException {
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

    private Expression postfixExpression() throws BaseParserException {
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

    private Expression primaryExpression() throws BaseParserException {
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
        } else if (isCurrentToken(TokenType.AT)) {
            match(TokenType.AT);

            Identifier identifier = new Identifier(mToken);
            match(TokenType.IDENTIFIER);

            match(TokenType.L_PAREN);

            ArgumentsList argumentsList = new ArgumentsList();

            if (!isCurrentToken(TokenType.R_PAREN)) {
                argumentsList = argumentsList();
            }

            match(TokenType.R_PAREN);

            return new ExternalCall(identifier, argumentsList);
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
                NewArray newArray = new NewArray(arrayType, conditionalExpression(), null);
                match(TokenType.R_SQ_PAREN);

                while (isCurrentToken(TokenType.L_SQ_PAREN)) {
                    match(TokenType.L_SQ_PAREN);
                    arrayType = new ArrayType(arrayType);
                    newArray = new NewArray(arrayType, conditionalExpression(), newArray);
                    match(TokenType.R_SQ_PAREN);
                }

                // Disable supporting multidimensional arrays.
                if (arrayType.getType().isArray()) {
                    throw new ArrayDimensionException(token);
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

    private BaseType extendedType() throws BaseParserException {
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

            // Disable supporting multidimensional arrays.
            if (arrayType.getType().isArray()) {
                throw new ArrayDimensionException(getPreviousToken());
            }

            type = arrayType;
        }

        return type;
    }

    private BaseType type() throws BaseParserException {
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

    private void match(TokenType expectedToken) throws BaseParserException {
        if (isCurrentToken(expectedToken)) {
            mPreviousToken = mToken;
            mToken = mLexer.getNextToken();
            return;
        }

        throw new UnexpectedTokenException(mToken);
    }

    private void match(TokenType... expectedToken) throws BaseParserException {
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

}
