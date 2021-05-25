package bee.lang.semanalysis;

import bee.lang.ast.*;
import bee.lang.ast.types.*;
import bee.lang.exceptions.TypeCheckingException;
import bee.lang.lexer.Token;
import bee.lang.symtable.*;
import bee.lang.visitors.TypeVisitor;

import java.util.Iterator;
import java.util.LinkedList;

public class TypeCheckingVisitor implements TypeVisitor {

    protected BaseScope mBaseScope;
    protected BaseScope mCurrentScope;
    protected ClassSymbol mCurrentClassSymbol;
    protected MethodSymbol mCurrentMethodSymbol;
    protected BaseScope mGlobalScope;
    private boolean isReturnStatement;
    private boolean isValidatingOtherConstructor;
    private boolean hasErrors;

    public TypeCheckingVisitor(BaseScope baseScope) {
        mBaseScope = baseScope;
        mCurrentScope = baseScope;
        mGlobalScope = baseScope;
        isReturnStatement = false;
        isValidatingOtherConstructor = false;
        hasErrors = false;
    }

    public void check(Program program) throws TypeCheckingException {
        visit(program);

        if (hasErrors) {
            throw new TypeCheckingException();
        }
    }

    @Override
    public BaseType visit(Add expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if (typeLeftExpression.isEqual(typeRightExpression) && (typeLeftExpression.isInt())) {
            return Type.Int;
        }

        printErrorMessage(expression.getToken(), "Operator '+' only accepts operands of type int. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(And expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if ((typeLeftExpression.isEqual(typeRightExpression)) && (typeLeftExpression.isBool())) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '&&' only accepts operands of type bool. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(ArrayAccess expression) {
        BaseType expressionType = expression.getExpression().visit(this);
        BaseType indexType = expression.getIndex().visit(this);

        if ((expressionType.isArray()) && (indexType.isInt())) {
            return ((ArrayType) expressionType).getType();
        }

        if (!expressionType.isArray()) {
            printErrorMessage(expression.getToken(), "Operator '[]' can be applied to array.");
        }

        if (!indexType.isInt()) {
            printErrorMessage(expression.getToken(), "Operator '[]' can be used with an index of type int.");
        }

        return Type.Error;
    }

    @Override
    public BaseType visit(Assignment expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        BaseType resultType = Type.Error;

        if (((typeLeftExpression.isInt()) || (typeLeftExpression.isChar()) || (typeLeftExpression.isBool()) || (typeLeftExpression.isArray())) &&
                (typeLeftExpression.isEqual(typeRightExpression))) {
            resultType = typeLeftExpression;
        }

        if (((typeLeftExpression.isClass()) || (typeLeftExpression.isArray())) && (typeRightExpression.isNil())) {
            resultType = typeLeftExpression;
        }

        if ((typeLeftExpression.isClass()) && (typeRightExpression.isClass())) {
            ClassType classTypeRightExpression = (ClassType) typeRightExpression;
            ClassType classTypeLeftExpression = (ClassType) typeLeftExpression;

            if (classTypeRightExpression.isSubclassOf(classTypeLeftExpression)) {
                resultType = typeLeftExpression;
            }
        }

        // Check lvalue and rvalue for an assignment statement.
        if ((!(expression.getLeftExpression() instanceof FieldAccess)) &&
                (!(expression.getLeftExpression() instanceof ArrayAccess)) &&
                (!(expression.getLeftExpression() instanceof Identifier))) {
            printErrorMessage(expression.getToken(), "Left part of the operator '=' is not lvalue.");
            return Type.Error;
        }

        if (expression.getLeftExpression() instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) expression.getLeftExpression();
            FieldSymbol fieldSymbol = ((FieldSymbol) fieldAccess.getSymbol());
            if (fieldSymbol.isConst()) {
                printErrorMessage(expression.getToken(), "Can not assign a value to the constant field '" + fieldAccess.getIdentifier().getName() + "'.");
                return Type.Error;
            }
        }

        if (expression.getLeftExpression() instanceof Identifier) {
            Identifier identifier = (Identifier) expression.getLeftExpression();

            if (identifier.getSymbol() instanceof ClassSymbol) {
                printErrorMessage(expression.getToken(), "Left part of the operator '=' is not lvalue.");
                return Type.Error;
            } else if (identifier.getSymbol() instanceof LocalVariableSymbol) {
                LocalVariableSymbol localVariableSymbol = ((LocalVariableSymbol) identifier.getSymbol());
                if (localVariableSymbol.isConst()) {
                    printErrorMessage(expression.getToken(), "Can not assign a value to the constant variable '" + identifier.getName() + "'.");
                    return Type.Error;
                }
            } else if (identifier.getSymbol() instanceof FieldSymbol) {
                FieldSymbol fieldSymbol = ((FieldSymbol) identifier.getSymbol());
                if (fieldSymbol.isConst()) {
                    printErrorMessage(expression.getToken(), "Can not assign a value to the constant field '" + identifier.getName() + "'.");
                    return Type.Error;
                }
            } else {
                return Type.Error;
            }
        }

        if (expression.getRightExpression() instanceof Super) {
            printErrorMessage(expression.getToken(), "Right part of the operator '=' is not rvalue.");
            return Type.Error;
        }

        if (resultType.isError()) {
            printErrorMessage(expression.getToken(), "Can not assign value of type '" + typeRightExpression + "' to variable of type '" + typeLeftExpression + "'.");
        }

        return resultType;
    }

    @Override
    public BaseType visit(AssignmentStatement statement) {
        statement.getExpression().visit(this);

        return Type.Nothing;
    }

    @Override
    public BaseType visit(Block statement) {
        mCurrentScope = statement.getScope();

        statement.getStatements().visit(this);

        mCurrentScope = mCurrentScope.getEnclosingScope();

        return Type.Nothing;
    }

    @Override
    public BaseType visit(BoolLiteral expression) {
        return Type.Bool;
    }

    @Override
    public BaseType visit(Break statement) {
        return Type.Nothing;
    }

    @Override
    public BaseType visit(Continue statement) {
        return Type.Nothing;
    }

    @Override
    public BaseType visit(Call expression) {
        BaseType expressionType = expression.getExpression().visit(this);

        if ((!expressionType.isClass()) && (!expressionType.isClassClass())) {
            printErrorMessage(expression.getIdentifier().getToken(), "Can not find a method '" + expression.getIdentifier().getName() + "'.");
            return Type.Error;
        }

        boolean isStaticExpectedMethod = expressionType.isClassClass();

        ClassSymbol classSymbol = (ClassSymbol) mGlobalScope.getSymbolInCurrentScope((isStaticExpectedMethod ? ((ClassClassType) expressionType).getClassType() : (ClassType) expressionType).getIdentifier().getName());

        MethodType expectedMethodType = new MethodType();

        Iterator<Expression> iterator = expression.getArgumentsList().getExpressionList().iterator();

        while (iterator.hasNext()) {
            expectedMethodType.addFormalArgumentType(iterator.next().visit(this));
        }

        LinkedList<MethodSymbol> foundMethodSymbols = new LinkedList<>();

        BaseScope scope = classSymbol;

        while (scope != null) {
            Symbol symbol = scope.getSymbolInCurrentScope(expression.getIdentifier().getName());

            if (symbol instanceof MethodSymbol) {
                while (symbol != null) {
                    MethodType foundMethodType = (MethodType) symbol.getType();

                    // Expected method and found method must be or must not be static.
                    if ((isStaticExpectedMethod == ((MethodSymbol) symbol).isStatic()) &&
                            (expectedMethodType.getFormalArgumentTypes().size() == foundMethodType.getFormalArgumentTypes().size())) {
                        Iterator<BaseType> expectedTypesIterator = expectedMethodType.getFormalArgumentTypes().iterator();
                        Iterator<BaseType> foundTypesIterator = foundMethodType.getFormalArgumentTypes().iterator();

                        boolean isSuitableMethod = true;

                        // Check every actual and formal parameter. Find an appropriate method.
                        while ((expectedTypesIterator.hasNext()) && (foundTypesIterator.hasNext())) {
                            BaseType expectedType = expectedTypesIterator.next();
                            BaseType foundType = foundTypesIterator.next();

                            if (expectedType.isNil()) {
                                if ((!foundType.isArray()) && (!foundType.isClass())) {
                                    isSuitableMethod = false;
                                    break;
                                }
                            } else {
                                if (((expectedType.isInt()) || (expectedType.isChar()) || (expectedType.isBool()) || (expectedType.isArray())) && (!foundType.isEqual(expectedType))) {
                                    isSuitableMethod = false;
                                    break;
                                }

                                if ((expectedType.isClass()) && (!((ClassType) expectedType).isSubclassOf((ClassType) foundType))) {
                                    isSuitableMethod = false;
                                    break;
                                }
                            }
                        }

                        // If a method exists in base class and has access modifier `public` or `protected` then it can be used in subclasses. Or if the method exists in the current class then everything is OK.
                        // Create candidate set of methods.
                        if ((isSuitableMethod) && ((scope == classSymbol) || ((((MethodSymbol) symbol).isPublic()) || (((MethodSymbol) symbol).isProtected())))) {
                            foundMethodSymbols.add((MethodSymbol) symbol);
                        }
                    }

                    symbol = symbol.getNextSymbol();
                }
            }

            scope = scope.getEnclosingScope();
        }

        MethodSymbol suitableMethod = findAppropriateMethod(foundMethodSymbols);

        if ((suitableMethod == null) && (!foundMethodSymbols.isEmpty())) {
            printErrorMessage(expression.getIdentifier().getToken(), "This call of the method '" + expression.getIdentifier().getName() + "' is ambiguous.");
            return Type.Error;
        }

        if (suitableMethod != null) {
            // Need to check where code calls this method.
            if (((suitableMethod.isPrivate()) || (suitableMethod.isProtected())) &&
                    (!(classSymbol.getType().isEqual(mCurrentClassSymbol.getType())))) {
                printErrorMessage(expression.getIdentifier().getToken(), "Can not get access to the method '" + suitableMethod.getIdentifier().getName() + "'.");
                return Type.Error;
            }

            expression.setSymbol(suitableMethod);

            if ((isValidatingOtherConstructor) && (!suitableMethod.isStatic())) {
                printErrorMessage(expression.getIdentifier().getToken(), "Cannot reference '" + suitableMethod.getIdentifier().getName() +"' in calling other constructor.");
                return Type.Error;
            }

            return ((MethodType) suitableMethod.getType()).getReturnType();
        } else {
            printErrorMessage(expression.getIdentifier().getToken(), "Can not find a method '" + expression.getIdentifier().getName() + "' for such arguments.");
            return Type.Error;
        }
    }

    @Override
    public BaseType visit(ExternalFunctionDeclaration statement) {
        Iterator<Statement> iterator = statement.getFormalArgumentsList().getStatementsList().iterator();

        while (iterator.hasNext()) {
            BaseType baseType = ((VariableDefinition) iterator.next()).getType();
            if ((!baseType.isInt()) && (!baseType.isBool()) && (!baseType.isChar())) {
                printErrorMessage(statement.getIdentifier().getToken(), "The external method '" + statement.getIdentifier().getName() + "' must have formal parameters of type 'int', 'bool' or 'char'.");
                return Type.Error;
            }
        }

        BaseType returnType = statement.getReturnType();

        if ((!returnType.isInt()) && (!returnType.isBool()) && (!returnType.isChar() && (!returnType.isVoid()))) {
            printErrorMessage(statement.getIdentifier().getToken(), "The external method '" + statement.getIdentifier().getName() + "' must return value of type 'int', 'bool' or 'char'.");
            return Type.Error;
        }

        return Type.Nothing;
    }

    @Override
    public BaseType visit(ExternalCall expression) {
        MethodType expectedMethodType = new MethodType();

        Iterator<Expression> iterator = expression.getArgumentsList().getExpressionList().iterator();

        while (iterator.hasNext()) {
            expectedMethodType.addFormalArgumentType(iterator.next().visit(this));
        }

        LinkedList<MethodSymbol> foundMethodSymbols = new LinkedList<>();

        Symbol symbol = mCurrentClassSymbol.getSymbolInCurrentScope(expression.getIdentifier().getName());

        if (symbol instanceof MethodSymbol) {
            while (symbol != null) {
                MethodType foundMethodType = (MethodType) symbol.getType();

                if ((expectedMethodType.getFormalArgumentTypes().size() == foundMethodType.getFormalArgumentTypes().size())) {
                    Iterator<BaseType> expectedTypesIterator = expectedMethodType.getFormalArgumentTypes().iterator();
                    Iterator<BaseType> foundTypesIterator = foundMethodType.getFormalArgumentTypes().iterator();

                    boolean isSuitableMethod = true;

                    // Check every actual and formal parameter. Find an appropriate external function.
                    while ((expectedTypesIterator.hasNext()) && (foundTypesIterator.hasNext())) {
                        BaseType expectedType = expectedTypesIterator.next();
                        BaseType foundType = foundTypesIterator.next();

                        if (((expectedType.isInt()) || (expectedType.isChar()) || (expectedType.isBool())) && (!foundType.isEqual(expectedType))) {
                            isSuitableMethod = false;
                            break;
                        }
                    }

                    if (isSuitableMethod) {
                        foundMethodSymbols.add((MethodSymbol) symbol);
                    }
                }

                symbol = symbol.getNextSymbol();
            }
        }

        MethodSymbol suitableMethod = findAppropriateMethod(foundMethodSymbols);

        if ((suitableMethod == null) && (!foundMethodSymbols.isEmpty())) {
            printErrorMessage(expression.getIdentifier().getToken(), "This call of the external function is ambiguous.");
            return Type.Error;
        }

        if (suitableMethod != null) {
            expression.setSymbol(suitableMethod);
            return ((MethodType) suitableMethod.getType()).getReturnType();
        } else {
            printErrorMessage(expression.getIdentifier().getToken(), "Can not find an external function for such arguments.");
            return Type.Error;
        }
    }

    @Override
    public BaseType visit(CharLiteral expression) {
        return Type.Char;
    }

    @Override
    public BaseType visit(ClassDefinition statement) {
        mCurrentClassSymbol = (ClassSymbol) statement.getSymbol();

        mCurrentScope = mCurrentClassSymbol;

        statement.getConstructorDefinitions().visit(this);
        statement.getMethodDefinitions().visit(this);
        statement.getFieldDefinitions().visit(this);
        statement.getExternalFunctionDeclarations().visit(this);

        mCurrentScope = mCurrentClassSymbol.getScope();

        mCurrentClassSymbol = null;

        return Type.Nothing;
    }

    @Override
    public BaseType visit(ConstructorDefinition statement) {
        mCurrentMethodSymbol = (MethodSymbol) statement.getSymbol();

        mCurrentScope = mCurrentMethodSymbol;

        isReturnStatement = false;

        checkOtherConstructors(statement);

        statement.getBody().visit(this);

        if (isReturnStatement) {
            printErrorMessage(statement.getToken(), "The keyword `return` inside of a constructor is not allowed.");
            return Type.Error;
        }

        mCurrentMethodSymbol = null;

        mCurrentScope = mCurrentScope.getEnclosingScope();

        return Type.Nothing;
    }

    @Override
    public BaseType visit(Div expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if (typeLeftExpression.isEqual(typeRightExpression) && (typeLeftExpression.isInt())) {
            return Type.Int;
        }

        printErrorMessage(expression.getToken(), "Operator '/' only accepts operands of type int. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(DoWhile statement) {
        BaseType typeConditionalExpression = statement.getExpression().visit(this);
        if (!typeConditionalExpression.isEqual(Type.Bool)) {
            printErrorMessage(statement.getToken(), "The statement 'do-while' supports condition of type bool.");
            return Type.Error;
        }

        Statement bodyStatement = statement.getStatement();

        mCurrentScope = bodyStatement.getScope();

        bodyStatement.visit(this);

        mCurrentScope = mCurrentScope.getEnclosingScope();

        return Type.Nothing;
    }

    @Override
    public BaseType visit(Equal expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if (((typeLeftExpression.isInt()) || (typeLeftExpression.isChar()) || (typeLeftExpression.isBool())) &&
                (typeLeftExpression.isEqual(typeRightExpression))) {
            return Type.Bool;
        }

        if ((typeLeftExpression.isClass()) && (typeRightExpression.isClass())) {
            ClassType classLeftExpression = ((ClassType) typeLeftExpression);
            ClassType classRightExpression = ((ClassType) typeRightExpression);

            if ((classLeftExpression.isSubclassOf(classRightExpression)) || (classRightExpression.isSubclassOf(classLeftExpression))) {
                return Type.Bool;
            }
        }

        if ((typeLeftExpression.isArray()) && (typeRightExpression.isArray())) {
            BaseType baseTypeLeftExpression = typeLeftExpression;
            BaseType baseTypeRightExpression = typeRightExpression;
            while ((baseTypeLeftExpression.isArray()) && (baseTypeRightExpression.isArray())) {
                baseTypeLeftExpression = ((ArrayType) baseTypeLeftExpression).getType();
                baseTypeRightExpression = ((ArrayType) baseTypeRightExpression).getType();
            }

            if (((baseTypeLeftExpression.isInt()) || (baseTypeLeftExpression.isChar()) || (baseTypeLeftExpression.isBool())) &&
                    (baseTypeLeftExpression.isEqual(baseTypeRightExpression))) {
                return Type.Bool;
            }

            if ((baseTypeLeftExpression.isClass()) && (baseTypeRightExpression.isClass())) {
                ClassType classLeftExpression = ((ClassType) baseTypeLeftExpression);
                ClassType classRightExpression = ((ClassType) baseTypeRightExpression);

                if ((classLeftExpression.isSubclassOf(classRightExpression)) || (classRightExpression.isSubclassOf(classLeftExpression))) {
                    return Type.Bool;
                }
            }
        }

        if ((((typeLeftExpression.isClass()) || (typeLeftExpression.isArray())) && (typeRightExpression.isNil())) ||
                ((typeLeftExpression.isNil()) && ((typeRightExpression.isClass()) || (typeRightExpression.isArray())))) {
            return Type.Bool;
        }

        if ((typeLeftExpression.isNil()) && (typeRightExpression.isNil())) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '==' only accepts operands of equal types.");

        return Type.Error;
    }

    @Override
    public BaseType visit(FieldAccess expression) {
        BaseType expressionType = expression.getExpression().visit(this);

        if ((expressionType.isClass()) || (expressionType.isClassClass())) {
            boolean isStaticField = expressionType.isClassClass();

            ClassSymbol classSymbol = (ClassSymbol) mGlobalScope.getSymbolInCurrentScope((isStaticField ? ((ClassClassType) expressionType).getClassType() : ((ClassType) expressionType)).getIdentifier().getName());

            BaseScope scope = classSymbol;

            while (scope != null) {
                Symbol symbol = scope.getSymbolInCurrentScope(expression.getIdentifier().getName());

                // Find a field in the current scope or in the base scope (class). The base scope must have the field with modifiers `public` or `protected`.
                if (symbol instanceof FieldSymbol) {
                    FieldSymbol fieldSymbol = (FieldSymbol) symbol;

                    if ((fieldSymbol.isStatic() == isStaticField) && ((scope == classSymbol) || ((fieldSymbol.isPublic()) || fieldSymbol.isProtected()))) {
                        // Need to check where code get access to this field.
                        if (((fieldSymbol.isPrivate()) || (fieldSymbol.isProtected())) &&
                                (!(classSymbol.getType().isEqual(mCurrentClassSymbol.getType())))) {
                            printErrorMessage(expression.getIdentifier().getToken(), "Can not get access to the field '" + symbol.getIdentifier().getName() + "'.");
                            return Type.Error;
                        }

                        expression.setSymbol(fieldSymbol);

                        return symbol.getType();
                    }
                }

                scope = scope.getEnclosingScope();
            }
        }

        printErrorMessage(expression.getIdentifier().getToken(), "Can not find the field '" + expression.getIdentifier().getName() + "'.");

        return Type.Error;
    }

    @Override
    public BaseType visit(FieldDefinition statement) {
        BaseType baseType = statement.getVariableDefinition().visit(this);

        if (statement.isStatic()) {
            Expression initExpression = statement.getVariableDefinition().getInitExpression();
            if (initExpression != null) {
                if (initExpression instanceof Call) {
                    MethodSymbol methodSymbol = (MethodSymbol) ((Call) initExpression).getSymbol();
                    if (!methodSymbol.isStatic()) {
                        printErrorMessage(statement.getVariableDefinition().getIdentifier().getToken(), "Can not use non-static method '" + methodSymbol.getIdentifier().getName() + "' in static context.");
                    }
                }

                if (initExpression instanceof FieldAccess) {
                    FieldSymbol fieldSymbol = (FieldSymbol) ((FieldAccess) initExpression).getSymbol();
                    if (!fieldSymbol.isStatic()) {
                        printErrorMessage(statement.getVariableDefinition().getIdentifier().getToken(), "Can not use non-static field '" + fieldSymbol.getIdentifier().getName() + "' in static context.");
                    }
                }

                if (initExpression instanceof Identifier) {
                    FieldSymbol fieldSymbol = (FieldSymbol) ((Identifier) initExpression).getSymbol();
                    if (!fieldSymbol.isStatic()) {
                        printErrorMessage(statement.getVariableDefinition().getIdentifier().getToken(), "Can not use non-static field '" + fieldSymbol.getIdentifier().getName() + "' in static context.");
                    }
                }
            }
        }

        return baseType;
    }

    @Override
    public BaseType visit(GreaterEqualThan expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if ((typeLeftExpression.isEqual(typeRightExpression)) && ((typeLeftExpression.isInt() || (typeLeftExpression.isChar())))) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '>=' only accepts operands of type int or char. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(GreaterThan expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if ((typeLeftExpression.isEqual(typeRightExpression)) && ((typeLeftExpression.isInt() || (typeLeftExpression.isChar())))) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '>' only accepts operands of type int or char. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(Identifier expression) {
        BaseScope scope = mCurrentScope;

        while (scope != null) {
            Symbol symbol = scope.getSymbolInCurrentScope(expression.getName());

            if (symbol instanceof FieldSymbol) {
                // Find a field in the current scope (class) or in the base scope (class). The base scope (class) must have the field with modifiers `public` or `protected`.
                FieldSymbol fieldSymbol = (FieldSymbol) symbol;

                if ((!fieldSymbol.isStatic()) && ((scope == mCurrentClassSymbol) || ((fieldSymbol.isPublic()) || fieldSymbol.isProtected()))) {
                    if ((mCurrentMethodSymbol != null) && (mCurrentMethodSymbol.isStatic())) {
                        printErrorMessage(expression.getToken(), "Static method '" + mCurrentMethodSymbol.getIdentifier().getName() + "' do not have access to '" + symbol.getIdentifier().getName() + "'.");
                        break;
                    }

                    if (isValidatingOtherConstructor) {
                        printErrorMessage(expression.getToken(), "Cannot reference '" + fieldSymbol.getIdentifier().getName() +"' in calling other constructor.");
                        break;
                    }

                    expression.setSymbol(fieldSymbol);

                    return symbol.getType();
                } else {
                    printErrorMessage(expression.getToken(), "Can not have access to the identifier '" + symbol.getIdentifier().getName() + "'.");
                    break;
                }
            }

            if (symbol instanceof LocalVariableSymbol) {
                expression.setSymbol(symbol);

                return symbol.getType();
            }

            if (symbol instanceof ClassSymbol) {
                expression.setSymbol(symbol);

                return symbol.getType();
            }

            scope = scope.getEnclosingScope();
        }

        return Type.Error;
    }

    @Override
    public BaseType visit(If statement) {
        BaseType typeConditionalExpression = statement.getExpression().visit(this);
        if (!typeConditionalExpression.isEqual(Type.Bool)) {
            printErrorMessage(statement.getToken(), "The statement 'if' supports condition of type bool.");
            return Type.Error;
        }

        Statement thenStatement = statement.getThenStatement();

        mCurrentScope = statement.getScope();

        thenStatement.visit(this);

        if (statement.getElseStatement() != null) {
            mCurrentScope = mCurrentScope.getEnclosingScope();

            Statement elseStatement = statement.getElseStatement();

            mCurrentScope = elseStatement.getScope();

            elseStatement.visit(this);
        }

        mCurrentScope = mCurrentScope.getEnclosingScope();

        return Type.Nothing;
    }

    @Override
    public BaseType visit(IntLiteral expression) {
        return Type.Int;
    }

    @Override
    public BaseType visit(LessEqualThan expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if ((typeLeftExpression.isEqual(typeRightExpression)) && ((typeLeftExpression.isInt() || (typeLeftExpression.isChar())))) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '<=' only accepts operands of type int or char. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(LessThan expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if ((typeLeftExpression.isEqual(typeRightExpression)) && ((typeLeftExpression.isInt() || (typeLeftExpression.isChar())))) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '<' only accepts operands of type int or char. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(MethodDefinition statement) {
        mCurrentMethodSymbol = (MethodSymbol) statement.getSymbol();

        mCurrentScope = mCurrentMethodSymbol;

        isReturnStatement = false;

        statement.getBody().visit(this);

        if ((!isReturnStatement) && (!((MethodType) mCurrentMethodSymbol.getType()).getReturnType().isEqual(Type.Void))) {
            printErrorMessage(statement.getIdentifier().getToken(), "Missing the keyword `return`.");
        }

        mCurrentMethodSymbol = null;

        mCurrentScope = mCurrentScope.getEnclosingScope();

        return Type.Nothing;
    }

    @Override
    public BaseType visit(Mod expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if (typeLeftExpression.isEqual(typeRightExpression) && (typeLeftExpression.isInt())) {
            return Type.Int;
        }

        printErrorMessage(expression.getToken(), "Operator '%' only accepts operands of type int. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(NewArray expression) {
        return expression.getType();
    }

    @Override
    public BaseType visit(NewObject expression) {
        BaseType expressionType = expression.getType();

        ClassSymbol currentClassSymbol = (ClassSymbol) mGlobalScope.getSymbolInCurrentScope(((ClassType) expressionType).getIdentifier().getName());

        MethodType expectedMethodType = new MethodType();

        Iterator<Expression> iterator = expression.getArgumentsList().getExpressionList().iterator();

        while (iterator.hasNext()) {
            expectedMethodType.addFormalArgumentType(iterator.next().visit(this));
        }

        LinkedList<MethodSymbol> foundMethodSymbols = new LinkedList<>();

        Symbol symbol = currentClassSymbol.getSymbolInCurrentScope("constructor");

        if (symbol instanceof MethodSymbol) {
            while (symbol != null) {
                MethodType foundMethodType = (MethodType) symbol.getType();

                if ((expectedMethodType.getFormalArgumentTypes().size() == foundMethodType.getFormalArgumentTypes().size())) {
                    Iterator<BaseType> expectedTypesIterator = expectedMethodType.getFormalArgumentTypes().iterator();
                    Iterator<BaseType> foundTypesIterator = foundMethodType.getFormalArgumentTypes().iterator();

                    boolean isSuitableMethod = true;

                    // Check every actual and formal parameter. Find an appropriate constructor.
                    while ((expectedTypesIterator.hasNext()) && (foundTypesIterator.hasNext())) {
                        BaseType expectedType = expectedTypesIterator.next();
                        BaseType foundType = foundTypesIterator.next();

                        if (expectedType.isNil()) {
                            if ((!foundType.isArray()) && (!foundType.isClass())) {
                                isSuitableMethod = false;
                                break;
                            }
                        } else {
                            if (((expectedType.isInt()) || (expectedType.isChar()) || (expectedType.isBool()) || (expectedType.isArray())) && (!foundType.isEqual(expectedType))) {
                                isSuitableMethod = false;
                                break;
                            }

                            if ((expectedType.isClass()) && (!((ClassType) expectedType).isSubclassOf((ClassType) foundType))) {
                                isSuitableMethod = false;
                                break;
                            }
                        }
                    }

                    if (isSuitableMethod) {
                        foundMethodSymbols.add((MethodSymbol) symbol);
                    }
                }

                symbol = symbol.getNextSymbol();
            }
        }

        MethodSymbol suitableMethod = findAppropriateMethod(foundMethodSymbols);

        if ((suitableMethod == null) && (!foundMethodSymbols.isEmpty())) {
            printErrorMessage(expression.getToken(), "This call of the constructor is ambiguous.");
            return Type.Error;
        }

        if (suitableMethod != null) {
            // Need to check where code calls this constructor.
            if (((suitableMethod.isPrivate()) || (suitableMethod.isProtected())) &&
                    (!(currentClassSymbol.getType().isEqual(mCurrentClassSymbol.getType())))) {
                printErrorMessage(expression.getToken(), "Can not get access to the constructor.");
                return Type.Error;
            }

            expression.setSymbol(suitableMethod);

            return ((MethodType) suitableMethod.getType()).getReturnType();
        } else {
            printErrorMessage(expression.getToken(), "Can not find a constructor for such arguments.");
            return Type.Error;
        }
    }

    @Override
    public BaseType visit(Nil expression) {
        return Type.Nil;
    }

    @Override
    public BaseType visit(Not expression) {
        BaseType type = expression.visit(this);

        if (type.isBool()) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Unary operator '!' only accepts operands of type bool.");

        return Type.Error;
    }

    @Override
    public BaseType visit(NotEqual expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if (((typeLeftExpression.isInt()) || (typeLeftExpression.isChar()) || (typeLeftExpression.isBool())) &&
                (typeLeftExpression.isEqual(typeRightExpression))) {
            return Type.Bool;
        }

        if ((typeLeftExpression.isClass()) && (typeRightExpression.isClass())) {
            ClassType classLeftExpression = ((ClassType) typeLeftExpression);
            ClassType classRightExpression = ((ClassType) typeRightExpression);

            if ((classLeftExpression.isSubclassOf(classRightExpression)) || (classRightExpression.isSubclassOf(classLeftExpression))) {
                return Type.Bool;
            }
        }

        if ((typeLeftExpression.isArray()) && (typeRightExpression.isArray())) {
            BaseType baseTypeLeftExpression = typeLeftExpression;
            BaseType baseTypeRightExpression = typeRightExpression;
            while ((baseTypeLeftExpression.isArray()) && (baseTypeRightExpression.isArray())) {
                baseTypeLeftExpression = ((ArrayType) baseTypeLeftExpression).getType();
                baseTypeRightExpression = ((ArrayType) baseTypeRightExpression).getType();
            }

            if (((baseTypeLeftExpression.isInt()) || (baseTypeLeftExpression.isChar()) || (baseTypeLeftExpression.isBool())) &&
                    (baseTypeLeftExpression.isEqual(baseTypeRightExpression))) {
                return Type.Bool;
            }

            if ((baseTypeLeftExpression.isClass()) && (baseTypeRightExpression.isClass())) {
                ClassType classLeftExpression = ((ClassType) baseTypeLeftExpression);
                ClassType classRightExpression = ((ClassType) baseTypeRightExpression);

                if ((classLeftExpression.isSubclassOf(classRightExpression)) || (classRightExpression.isSubclassOf(classLeftExpression))) {
                    return Type.Bool;
                }
            }
        }

        if ((((typeLeftExpression.isClass()) || (typeLeftExpression.isArray())) && (typeRightExpression.isNil())) ||
                ((typeLeftExpression.isNil()) && ((typeRightExpression.isClass()) || (typeRightExpression.isArray())))) {
            return Type.Bool;
        }

        if ((typeLeftExpression.isNil()) && (typeRightExpression.isNil())) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '!=' only accepts operands of type int, char, bool and references to arrays and objects. Both operands must have the same type (operands of reference types must be both arrays or objects).");

        return Type.Error;
    }

    @Override
    public BaseType visit(Or expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if ((typeLeftExpression.isEqual(typeRightExpression)) && (typeLeftExpression.isBool())) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '||' only accepts operands of type bool. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(Program statement) {
        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }

        return Type.Nothing;
    }

    @Override
    public BaseType visit(Return statement) {
        isReturnStatement = true;

        BaseType typeExpression = (statement.getExpression() == null ? Type.Void : statement.getExpression().visit(this));

        BaseType returnType = ((MethodType) mCurrentMethodSymbol.getType()).getReturnType();

        if (typeExpression.isNil()) {
            if ((!returnType.isClass()) && (!returnType.isArray())) {
                printErrorMessage(statement.getToken(), "The keyword `nil` can not be used with types 'int', 'bool' and 'char'.");
                return Type.Error;
            }
        } else if (typeExpression.isVoid()) {
            if (!returnType.isVoid()) {
                printErrorMessage(statement.getToken(), "Method must return a value.");
                return Type.Error;
            }
        } else if (typeExpression.isClass()) {
            if ((!returnType.isClass()) || (!((ClassType) typeExpression).isSubclassOf((ClassType) returnType))) {
                printErrorMessage(statement.getToken(), "Provided type : '" + typeExpression + "' is not a subclass of required type : '" + returnType + "'.");
                return Type.Error;
            }
        } else {
            if (!typeExpression.isEqual(returnType)) {
                printErrorMessage(statement.getToken(), "Provided type : '" + typeExpression + "', but required type : '" + returnType + "'.");
                return Type.Error;
            }
        }

        return Type.Nothing;
    }

    @Override
    public BaseType visit(Statements statement) {
        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }

        return Type.Nothing;
    }

    @Override
    public BaseType visit(StringLiteral expression) {
        return new ArrayType(Type.Char);
    }

    @Override
    public BaseType visit(Subtract expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if (typeLeftExpression.isEqual(typeRightExpression) && (typeLeftExpression.isInt())) {
            return Type.Int;
        }

        printErrorMessage(expression.getToken(), "Operator '-' only accepts operands of type int. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(Super expression) {
        if ((mCurrentMethodSymbol != null) && (mCurrentMethodSymbol.isStatic())) {
            printErrorMessage(expression.getToken(), "`super` can not be used in the static method.");

            return Type.Error;
        }

        // The current class must have the base class to return type of the reference `super`.
        if (mCurrentClassSymbol.getEnclosingScope() instanceof ClassSymbol) {
            return ((ClassClassType) ((ClassSymbol) mCurrentClassSymbol.getEnclosingScope()).getType()).getClassType();
        }

        return Type.Error;
    }

    @Override
    public BaseType visit(TernaryOperator expression) {
        BaseType typeConditionalExpression = expression.getConditionalExpression().visit(this);
        BaseType typeThenExpression = expression.getThenExpression().visit(this);
        BaseType typeElseExpression = expression.getElseExpression().visit(this);

        if (typeConditionalExpression.isBool()) {
            if (typeThenExpression.isClass()) {
                ClassType lub = ((ClassType) typeThenExpression).lub((ClassType) typeElseExpression);
                if (lub != null) {
                    return lub;
                } else {
                    printErrorMessage(expression.getToken(), "Ternary operator '<condition> ? <then branch> : <else branch>' only accepts branches of the same type. The branch `then` and `else` must have an expression of the same type.");
                }
            } else {
                if (typeThenExpression.isEqual(typeElseExpression)) {
                    return typeThenExpression;
                } else {
                    printErrorMessage(expression.getToken(), "Ternary operator '<condition> ? <then branch> : <else branch>' only accepts branches of the same type. The branch `then` and `else` must have an expression of the same type.");
                }
            }
        } else {
            printErrorMessage(expression.getToken(), "Ternary operator '? :' only accepts condition of type bool.");
        }

        return Type.Error;
    }

    @Override
    public BaseType visit(This expression) {
        if ((mCurrentMethodSymbol != null) && (mCurrentMethodSymbol.isStatic())) {
            printErrorMessage(expression.getToken(), "`this` can not be used in the static method.");

            return Type.Error;
        }

        return ((ClassClassType) mCurrentClassSymbol.getType()).getClassType();
    }

    @Override
    public BaseType visit(Times expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if (typeLeftExpression.isEqual(typeRightExpression) && (typeLeftExpression.isInt())) {
            return Type.Int;
        }

        printErrorMessage(expression.getToken(), "Operator '*' only accepts operands of type int. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(UnaryMinus expression) {
        BaseType type = expression.getExpression().visit(this);

        if (type.isInt()) {
            return Type.Int;
        }

        printErrorMessage(expression.getToken(), "Unary operator '-' only accepts operands of type int.");

        return Type.Error;
    }

    @Override
    public BaseType visit(VariableDefinition statement) {
        if (statement.getInitExpression() != null) {
            BaseType typeVariable = statement.getType();
            BaseType typeInitExpression = statement.getInitExpression().visit(this);

            if (((typeVariable.isInt()) || (typeVariable.isChar()) || (typeVariable.isBool()) || (typeVariable.isArray())) &&
                    (typeVariable.isEqual(typeInitExpression))) {
                return Type.Nothing;
            }

            if (((typeVariable.isClass()) || (typeVariable.isArray())) && (typeInitExpression.isNil())) {
                return Type.Nothing;
            }

            if ((typeVariable.isClass()) && (typeInitExpression.isClass())) {
                ClassType classTypeVariable = (ClassType) typeVariable;
                ClassType classTypeInitExpression = (ClassType) typeInitExpression;

                if (classTypeInitExpression.isSubclassOf(classTypeVariable)) {
                    return Type.Nothing;
                }
            }

            // Check rvalue to initialize a variable.
            if (statement.getInitExpression() instanceof Super) {
                printErrorMessage(statement.getToken(), "Right part of the operator '=' is not rvalue.");
                return Type.Error;
            }

            printErrorMessage(statement.getToken(), "Can not assign value of type '" + typeInitExpression + "' to variable of type '" + typeVariable + "'.");

            return Type.Error;
        }

        return Type.Nothing;
    }

    @Override
    public BaseType visit(While statement) {
        BaseType typeConditionalExpression = statement.getExpression().visit(this);
        if (!typeConditionalExpression.isEqual(Type.Bool)) {
            printErrorMessage(statement.getToken(), "The statement 'while' supports condition of type bool.");
            return Type.Error;
        }

        Statement bodyStatement = statement.getStatement();

        mCurrentScope = bodyStatement.getScope();

        bodyStatement.visit(this);

        mCurrentScope = mCurrentScope.getEnclosingScope();

        return Type.Nothing;
    }

    private void checkOtherConstructors(ConstructorDefinition statement) {
        // Bee does not have the root class for all classes by default. If the keyword `super` uses in a constructor of a class without a base class then need to ignore this case and do not find constructors in a base class.
        if ((statement.getSuperConstructorArgumentsList() != null) && (mCurrentClassSymbol.getBaseClassIdentifier() == null)) {
            if (!statement.getSuperConstructorArgumentsList().getExpressionList().isEmpty()) {
                printErrorMessage(statement.getToken(), "A class without a base class can not call super-constructor with arguments.");
            }

            return;
        }

        MethodType expectedMethodType = new MethodType();

        Iterator<Expression> iterator = (statement.getSuperConstructorArgumentsList() != null ?
                statement.getSuperConstructorArgumentsList() : statement.getOtherConstructorArgumentsList()).getExpressionList().iterator();

        while (iterator.hasNext()) {
            isValidatingOtherConstructor = true;
            expectedMethodType.addFormalArgumentType(iterator.next().visit(this));
            isValidatingOtherConstructor = false;
        }

        expectedMethodType.addReturnType(((ClassClassType) mCurrentClassSymbol.getType()).getClassType());

        LinkedList<MethodSymbol> foundMethodSymbols = new LinkedList<>();

        BaseScope scope = statement.getSuperConstructorArgumentsList() != null ? mCurrentClassSymbol.getEnclosingScope() : mCurrentClassSymbol;

        Symbol symbol = scope.getSymbolInCurrentScope("constructor");

        if (symbol instanceof MethodSymbol) {
            while (symbol != null) {
                MethodType foundMethodType = (MethodType) symbol.getType();

                // Expected method and found method must be or must not be static
                if ((expectedMethodType.getFormalArgumentTypes().size() == foundMethodType.getFormalArgumentTypes().size())) {
                    Iterator<BaseType> expectedTypesIterator = expectedMethodType.getFormalArgumentTypes().iterator();
                    Iterator<BaseType> foundTypesIterator = foundMethodType.getFormalArgumentTypes().iterator();

                    boolean isSuitableMethod = true;

                    // Check every actual and formal parameter. Find an appropriate method.
                    while ((expectedTypesIterator.hasNext()) && (foundTypesIterator.hasNext())) {
                        BaseType expectedType = expectedTypesIterator.next();
                        BaseType foundType = foundTypesIterator.next();

                        if (expectedType.isNil()) {
                            if ((!foundType.isArray()) && (!foundType.isClass())) {
                                isSuitableMethod = false;
                                break;
                            }
                        } else {
                            if (((expectedType.isInt()) || (expectedType.isChar()) || (expectedType.isBool()) || (expectedType.isArray())) && (!foundType.isEqual(expectedType))) {
                                isSuitableMethod = false;
                                break;
                            }

                            if ((expectedType.isClass()) && (!((ClassType) expectedType).isSubclassOf((ClassType) foundType))) {
                                isSuitableMethod = false;
                                break;
                            }
                        }
                    }

                    // If a method exists in base class and has access modifier `public` or `protected` then it can be used in subclasses. Or if the method exists in the current class then everything is OK.
                    if ((isSuitableMethod) && ((scope == mCurrentClassSymbol) || ((((MethodSymbol) symbol).isPublic()) || (((MethodSymbol) symbol).isProtected())))) {
                        foundMethodSymbols.add((MethodSymbol) symbol);
                    }
                }

                symbol = symbol.getNextSymbol();
            }
        }

        MethodSymbol suitableMethod = findAppropriateMethod(foundMethodSymbols);

        if ((suitableMethod == null) && (!foundMethodSymbols.isEmpty())) {
            printErrorMessage(statement.getToken(), "This call of the constructor is ambiguous.");
            return;
        }

        if (suitableMethod != null) {
            statement.setOtherConstructorSymbol(suitableMethod);
        } else {
            printErrorMessage(statement.getToken(), "Can not find a constructor for such arguments.");
        }
    }

    private MethodSymbol findAppropriateMethod(LinkedList<MethodSymbol> foundMethodSymbols) {
        // Use next rule to choose a function.
        // Suppose two candidate methods function1(i1, i2, i3, .... , in) and function2(j1, j2, j3, .... , jn).
        // function1 is the best match than function2 if ik <= jk for all k from 1 to n (e.g. i1 <= j1 and i2 <= j2 and i3 <= j3 and in <= jn).
        //
        // This case is ambiguous:
        // class A { ... }
        // class B : A { ... }
        // class C { f1() { f(new B(), new B()) } ... f(var a : A, var b : B) { ... } f(var b : B, var a : A) { ... } ... }
        //
        // Need to check twice f(var a : A, var b : B) with f(var b : B, var a : A) and f(var b : B, var a : A) with f(var a : A, var b : B).
        // In this case f(var a : A, var b : B) and f(var b : B, var a : A) does not satisfy the rule above.
        MethodSymbol suitableMethod = null;
        Iterator<MethodSymbol> methodSymbolIterator = foundMethodSymbols.iterator();

        if (methodSymbolIterator.hasNext()) {
            suitableMethod = methodSymbolIterator.next();
            while (methodSymbolIterator.hasNext()) {
                MethodSymbol methodSymbol = methodSymbolIterator.next();

                MethodType suitableMethodType = (MethodType) suitableMethod.getType();
                MethodType methodType = (MethodType) methodSymbol.getType();

                Iterator<BaseType> suitableMethodTypesIterator = suitableMethodType.getFormalArgumentTypes().iterator();
                Iterator<BaseType> methodTypesIterator = methodType.getFormalArgumentTypes().iterator();

                boolean isSuitableMethod = true;

                // Compare arguments of function1 and function2.
                while ((suitableMethodTypesIterator.hasNext()) && (methodTypesIterator.hasNext())) {
                    BaseType suitableType = suitableMethodTypesIterator.next();
                    BaseType type = methodTypesIterator.next();

                    if ((suitableType.isClass()) && (type.isClass())) {
                        if (!((ClassType) suitableType).isSubclassOf((ClassType) type)) {
                            isSuitableMethod = false;
                            break;
                        }
                    }
                }

                if (!isSuitableMethod) {
                    suitableMethodTypesIterator = suitableMethodType.getFormalArgumentTypes().iterator();
                    methodTypesIterator = methodType.getFormalArgumentTypes().iterator();

                    isSuitableMethod = true;

                    // Compare arguments of function2 and function1.
                    while ((suitableMethodTypesIterator.hasNext()) && (methodTypesIterator.hasNext())) {
                        BaseType suitableType = suitableMethodTypesIterator.next();
                        BaseType type = methodTypesIterator.next();

                        if ((suitableType.isClass()) && (type.isClass())) {
                            if (!((ClassType) type).isSubclassOf((ClassType) suitableType)) {
                                isSuitableMethod = false;
                                break;
                            }
                        }
                    }

                    if (isSuitableMethod) {
                        suitableMethod = methodSymbol;
                    } else {
                        return null;
                    }
                }
            }
        }

        return suitableMethod;
    }

    protected void printErrorMessage(Token token, String message) {
        hasErrors = true;
        System.out.println("[ " + token.getFileName() + " : " + token.getLine() + " ] " + message);
    }

}
