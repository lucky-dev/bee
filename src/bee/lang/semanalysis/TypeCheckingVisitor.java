package bee.lang.semanalysis;

import bee.lang.ast.*;
import bee.lang.ast.types.*;
import bee.lang.lexer.Token;
import bee.lang.symtable.*;
import bee.lang.visitors.TypeVisitor;

import java.util.Iterator;

public class TypeCheckingVisitor implements TypeVisitor {

    protected BaseScope mBaseScope;
    protected BaseScope mCurrentScope;
    protected ClassSymbol mCurrentClassSymbol;
    protected MethodSymbol mCurrentMethodSymbol;
    protected BaseScope mGlobalScope;
    private boolean isConst;
    private boolean isReturnStatement;

    public TypeCheckingVisitor(BaseScope baseScope) {
        mBaseScope = baseScope;
        mCurrentScope = baseScope;
        mGlobalScope = baseScope;
        isConst = false;
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

        if ((expression.getLeftExpression() instanceof FieldAccess) && (isConst)) {
            isConst = false;
            FieldAccess fieldAccess = (FieldAccess) expression.getLeftExpression();
            printErrorMessage(expression.getToken(), "Can not assign a value to constant field '" + fieldAccess.getIdentifier().getName() + "'.");
            return Type.Error;
        }

        if ((expression.getLeftExpression() instanceof Identifier) && (isConst)) {
            isConst = false;
            Identifier identifier = (Identifier) expression.getLeftExpression();
            printErrorMessage(expression.getToken(), "Can not assign a value to constant variable '" + identifier.getName() + "'.");
            return Type.Error;
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
        statement.getStatements().visit(this);

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

        MethodSymbol foundMethodSymbol = null;

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
                        if ((isSuitableMethod) && ((scope == classSymbol) || ((((MethodSymbol) symbol).isPublic()) || (((MethodSymbol) symbol).isProtected())))) {
                            // Found method may be inherited method or overridden. If method is inherited then need to check formal arguments to prevent clashing of methods.
                            // If method is overridden then everything is OK. Keep searching of other methods.
                            if (foundMethodSymbol == null) {
                                foundMethodSymbol = (MethodSymbol) symbol;
                            } else {
                                // Another method with the same formal parameters are found. It is an error.
                                if (!expectedMethodType.isEqualFormalArguments((MethodType) foundMethodSymbol.getType())) {
                                    printErrorMessage(expression.getIdentifier().getToken(), "Clash of methods.");
                                    return Type.Error;
                                }
                            }
                        }
                    }

                    symbol = symbol.getNextSymbol();
                }
            }

            scope = scope.getEnclosingScope();
        }

        if (foundMethodSymbol != null) {
            // Need to check where code calls this method.
            if (foundMethodSymbol.isProtected()) {
                // Only the current class and subclasses have access to protected methods.
                if (!(((ClassClassType) mCurrentClassSymbol.getType()).getClassType().isSubclassOf(((ClassClassType) classSymbol.getType()).getClassType()))) {
                    printErrorMessage(expression.getIdentifier().getToken(), "Can not get access to the method '" + foundMethodSymbol.getIdentifier().getName() + "'.");
                    return Type.Error;
                }
            }

            if (foundMethodSymbol.isPrivate()) {
                // Only the current class has access to private methods.
                if (!((((ClassClassType) classSymbol.getType()).getClassType()).isEqual(((ClassClassType) mCurrentClassSymbol.getType()).getClassType()))) {
                    printErrorMessage(expression.getIdentifier().getToken(), "Can not get access to the method '" + foundMethodSymbol.getIdentifier().getName() + "'.");
                    return Type.Error;
                }
            }

            expression.setSymbol(foundMethodSymbol);

            return ((MethodType) foundMethodSymbol.getType()).getReturnType();
        } else {
            printErrorMessage(expression.getIdentifier().getToken(), "Can not find a method '" + expression.getIdentifier().getName() + "' for such arguments.");
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

        mCurrentScope = mCurrentClassSymbol.getScope();

        mCurrentClassSymbol = null;

        return Type.Nothing;
    }

    @Override
    public BaseType visit(ConstructorDefinition statement) {
        mCurrentMethodSymbol = (MethodSymbol) statement.getSymbol();

        mCurrentScope = mCurrentMethodSymbol;

        isReturnStatement = false;

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

        statement.getStatement().visit(this);

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

        if (((typeLeftExpression.isClass()) && (typeRightExpression.isClass())) ||
                ((typeLeftExpression.isArray()) && (typeRightExpression.isArray()))) {
            return Type.Bool;
        }

        if ((((typeLeftExpression.isClass()) || (typeLeftExpression.isArray())) && (typeRightExpression.isNil())) ||
                ((typeLeftExpression.isNil()) && ((typeRightExpression.isClass()) || (typeRightExpression.isArray())))) {
            return Type.Bool;
        }

        if ((typeLeftExpression.isNil()) && (typeRightExpression.isNil())) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '==' only accepts operands of type int, char, bool and references to arrays and objects. Both operands must have the same type (operands of reference types must be both arrays or objects).");

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
                        if (fieldSymbol.isProtected()) {
                            // Only the current class and subclasses have access to protected fields
                            if (!(((ClassClassType) mCurrentClassSymbol.getType()).getClassType().isSubclassOf(((ClassClassType) classSymbol.getType()).getClassType()))) {
                                printErrorMessage(expression.getIdentifier().getToken(), "Can not get access to the field '" + symbol.getIdentifier().getName() + "'.");
                                return Type.Error;
                            }
                        }

                        if (fieldSymbol.isPrivate()) {
                            // Only the current class has access to private fields
                            if (!((((ClassClassType) classSymbol.getType()).getClassType()).isEqual(((ClassClassType) mCurrentClassSymbol.getType()).getClassType()))) {
                                printErrorMessage(expression.getIdentifier().getToken(), "Can not get access to the field '" + symbol.getIdentifier().getName() + "'.");
                                return Type.Error;
                            }
                        }

                        isConst = fieldSymbol.isConst();

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
        return statement.getVariableDefinition().visit(this);
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
                    if (mCurrentMethodSymbol.isStatic()) {
                        printErrorMessage(expression.getToken(), "Static method '" + mCurrentMethodSymbol.getIdentifier().getName() + "' do not have access to '" + symbol.getIdentifier().getName() + "'.");
                        break;
                    }

                    isConst = fieldSymbol.isConst();

                    expression.setSymbol(fieldSymbol);

                    return symbol.getType();
                } else {
                    printErrorMessage(expression.getToken(), "Can not have access to the identifier '" + symbol.getIdentifier().getName() + "'.");
                    break;
                }
            } else if (symbol instanceof LocalVariableSymbol) {
                isConst = ((LocalVariableSymbol) symbol).isConst();

                expression.setSymbol(symbol);

                return symbol.getType();
            } else {
                if (symbol != null) {
                    return symbol.getType();
                }
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

        statement.getThenStatement().visit(this);

        if (statement.getElseStatement() != null) {
            statement.getElseStatement().visit(this);
        }

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

        MethodSymbol foundMethodSymbol = null;

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
                        if (foundMethodSymbol == null) {
                            foundMethodSymbol = (MethodSymbol) symbol;
                        } else {
                            // Another constructor with the same formal parameters are found. It is an error.
                            if (!expectedMethodType.isEqualFormalArguments((MethodType) foundMethodSymbol.getType())) {
                                printErrorMessage(expression.getToken(), "Clash of constructors.");
                                return Type.Error;
                            }
                        }
                    }
                }

                symbol = symbol.getNextSymbol();
            }
        }

        if (foundMethodSymbol != null) {
            // Need to check where code calls this constructor.
            if (foundMethodSymbol.isProtected()) {
                // Only the current class and subclasses have access to protected constructors.
                if (!(((ClassClassType) mCurrentClassSymbol.getType()).getClassType().isSubclassOf(((ClassClassType) currentClassSymbol.getType()).getClassType()))) {
                    printErrorMessage(expression.getToken(), "Can not get access to the constructor.");
                    return Type.Error;
                }
            }

            if (foundMethodSymbol.isPrivate()) {
                // Only the current class has access to private constructors.
                if (!((((ClassClassType) currentClassSymbol.getType()).getClassType()).isEqual(((ClassClassType) mCurrentClassSymbol.getType()).getClassType()))) {
                    printErrorMessage(expression.getToken(), "Can not get access to the constructor.");
                    return Type.Error;
                }
            }

            expression.setSymbol(foundMethodSymbol);

            return ((MethodType) foundMethodSymbol.getType()).getReturnType();
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

        if (((typeLeftExpression.isClass()) && (typeRightExpression.isClass())) ||
                ((typeLeftExpression.isArray()) && (typeRightExpression.isArray()))) {
            return Type.Bool;
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
        BaseType type = expression.visit(this);

        if (type.isInt()) {
            return Type.Int;
        }

        printErrorMessage(expression.getToken(), "Unary operator '-' only accepts operands of type int.");

        return Type.Error;
    }

    @Override
    public BaseType visit(VariableDefinition statement) {
        BaseType resultType = Type.Error;

        if (statement.getInitExpression() != null) {
            BaseType typeVariable = statement.getType();
            BaseType typeInitExpression = statement.getInitExpression().visit(this);

            if (((typeVariable.isInt()) || (typeVariable.isChar()) || (typeVariable.isBool()) || (typeVariable.isArray())) &&
                    (typeVariable.isEqual(typeInitExpression))) {
                resultType = Type.Nothing;
            }

            if (((typeVariable.isClass()) || (typeVariable.isArray())) && (typeInitExpression.isNil())) {
                resultType = Type.Nothing;
            }

            if ((typeVariable.isClass()) && (typeInitExpression.isClass())) {
                ClassType classTypeVariable = (ClassType) typeVariable;
                ClassType classTypeInitExpression = (ClassType) typeInitExpression;

                if (classTypeInitExpression.isSubclassOf(classTypeVariable)) {
                    resultType = Type.Nothing;
                }
            }

            // Check rvalue to initialize a variable.
            if (statement.getInitExpression() instanceof Super) {
                printErrorMessage(statement.getToken(), "Right part of the operator '=' is not rvalue.");
                return resultType;
            }

            if (resultType.isError()) {
                printErrorMessage(statement.getToken(), "Can not assign value of type '" + typeInitExpression + "' to variable of type '" + typeVariable + "'.");
            }
        }

        return resultType;
    }

    @Override
    public BaseType visit(While statement) {
        BaseType typeConditionalExpression = statement.getExpression().visit(this);
        if (!typeConditionalExpression.isEqual(Type.Bool)) {
            printErrorMessage(statement.getToken(), "The statement 'while' supports condition of type bool.");
            return Type.Error;
        }

        statement.getStatement().visit(this);

        return Type.Nothing;
    }

    protected void printErrorMessage(Token token, String message) {
        System.out.println("[ " + token.getFileName() + " : " + token.getLine() + " ] " + message);
    }

}
