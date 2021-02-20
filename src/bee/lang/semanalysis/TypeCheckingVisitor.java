package bee.lang.semanalysis;

import bee.lang.ast.*;
import bee.lang.ast.types.*;
import bee.lang.lexer.Token;
import bee.lang.symtable.*;
import bee.lang.visitors.TypeVisitor;

import java.util.Iterator;
import java.util.LinkedList;

public class TypeCheckingVisitor implements TypeVisitor {

    private BaseScope mBaseScope;
    private BaseScope mCurrentScope;
    private ClassSymbol mCurrentClassSymbol;
    private BaseScope mGlobalScope;

    public TypeCheckingVisitor(BaseScope baseScope) {
        mBaseScope = baseScope;
        mCurrentScope = baseScope;
        mGlobalScope = baseScope;
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

        if (((typeLeftExpression.isInt()) || (typeLeftExpression.isChar()) || (typeLeftExpression.isBool()) || (typeLeftExpression.isArray())) &&
                (typeLeftExpression.isEqual(typeRightExpression))) {
            return typeLeftExpression;
        }

        if (((typeLeftExpression.isClass()) || (typeLeftExpression.isArray())) && (typeRightExpression.isNil())) {
            return typeLeftExpression;
        }

        if ((typeLeftExpression.isClass()) && (typeRightExpression.isClass())) {
            ClassType classTypeRightExpression = (ClassType) typeRightExpression;
            ClassType classTypeLeftExpression = (ClassType) typeLeftExpression;

            if (classTypeRightExpression.isSubclassOf(classTypeLeftExpression)) {
                return typeLeftExpression;
            }
        }

        printErrorMessage(expression.getToken(), "Can not assign value of type '" + typeRightExpression + "' to variable of type '" + typeLeftExpression + "'.");

        return Type.Error;
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

            if ((symbol != null) && (symbol instanceof MethodSymbol)) {
                while (symbol != null) {
                    MethodType foundMethodType = (MethodType) symbol.getType();

                    // Expected method and found method must be or must not be static
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

                        // If a method exists in base class and has access modifier `public` or `protected`. It can be used in subclasses. Or if the method exists in the current class then everything is OK.
                        if ((isSuitableMethod) && ((scope == mCurrentScope) || ((((MethodSymbol) symbol).isPublic()) || (((MethodSymbol) symbol).isProtected())))) {
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

        if (foundMethodSymbol == null) {
            printErrorMessage(expression.getIdentifier().getToken(), "Can not find a method '" + expression.getIdentifier().getName() + "' for such arguments.");
        }

        return foundMethodSymbol != null ? ((MethodType) foundMethodSymbol.getType()).getReturnType() : Type.Error;
    }

    @Override
    public BaseType visit(CharLiteral expression) {
        return Type.Char;
    }

    @Override
    public BaseType visit(ClassDefinition statement) {
        mCurrentClassSymbol = (ClassSymbol) mBaseScope.getSymbolInCurrentScope(statement.getClassIdentifier().getName());

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
        MethodType constructorType = new MethodType();

        Iterator<Statement> iterator = statement.getFormalArgumentsList().getStatementsList().iterator();

        while (iterator.hasNext()) {
            VariableDefinition variableDefinition = (VariableDefinition) iterator.next();
            constructorType.addFormalArgumentType(variableDefinition.getType());
        }

        constructorType.addReturnType(Type.Class(((ClassSymbol) mCurrentScope).getIdentifier()));

        Symbol symbol = mCurrentScope.getSymbolInCurrentScope("constructor");

        MethodSymbol currentConstructor = null;

        Symbol constructor = symbol;

        while (constructor != null) {
            if ((statement.getAccessModifier() == ((MethodSymbol) constructor).getAccessModifier()) &&
                    (constructorType.isEqual(constructor.getType()))) {
                currentConstructor = (MethodSymbol) constructor;
                break;
            }

            constructor = constructor.getNextSymbol();
        }

        mCurrentScope = currentConstructor;

        statement.getBody().visit(this);

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
        return Type.Nothing;
    }

    @Override
    public BaseType visit(Equal expression) {
        BaseType typeLeftExpression = expression.getLeftExpression().visit(this);
        BaseType typeRightExpression = expression.getRightExpression().visit(this);

        if (((typeLeftExpression.isInt()) || (typeLeftExpression.isChar()) || (typeLeftExpression.isBool()) || (typeLeftExpression.isArray()) || (typeLeftExpression.isClass())) &&
                (typeLeftExpression.isEqual(typeRightExpression))) {
            return Type.Bool;
        }

        if ((((typeLeftExpression.isClass()) || (typeLeftExpression.isArray())) && (typeRightExpression.isNil())) ||
                ((typeLeftExpression.isNil()) && ((typeRightExpression.isClass()) || (typeRightExpression.isArray())))) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '==' only accepts operands of type int, char, bool and references to arrays and objects. Both operands must have the same type.");

        return Type.Error;
    }

    @Override
    public BaseType visit(FieldAccess expression) {
        BaseType expressionType = expression.getExpression().visit(this);

        if (expressionType.isClass()) {
            BaseScope scope = (ClassSymbol) mGlobalScope.getSymbolInCurrentScope(((ClassType) expressionType).getIdentifier().getName());

            while (scope != null) {
                Symbol symbol = scope.getSymbolInCurrentScope(expression.getIdentifier().getName());

                if (symbol instanceof FieldSymbol) {
                    return symbol.getType();
                }

                scope = scope.getEnclosingScope();
            }
        }

        return Type.Error;
    }

    @Override
    public BaseType visit(FieldDefinition statement) {
        VariableDefinition variableDefinitionStatement = statement.getVariableDefinition();

        if (variableDefinitionStatement.getInitExpression() != null) {
            BaseType typeVariable = variableDefinitionStatement.getType();
            BaseType typeInitExpression = variableDefinitionStatement.getInitExpression().visit(this);

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

            printErrorMessage(statement.getToken(), "Can not assign value of type '" + typeInitExpression + "' to variable of type '" + typeVariable + "'.");
        }

        return Type.Error;
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
        Symbol symbol = mCurrentScope.getSymbol(expression.getName());

        return symbol != null ? symbol.getType() : Type.Error;
    }

    @Override
    public BaseType visit(If statement) {
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
        MethodType methodType = new MethodType();

        Iterator<Statement> iterator = statement.getFormalArgumentsList().getStatementsList().iterator();

        while (iterator.hasNext()) {
            VariableDefinition variableDefinition = (VariableDefinition) iterator.next();
            methodType.addFormalArgumentType(variableDefinition.getType());
        }

        methodType.addReturnType(statement.getReturnType());

        Symbol symbol = mCurrentScope.getSymbolInCurrentScope(statement.getIdentifier().getName());

        MethodSymbol currentMethod = null;

        Symbol method = symbol;

        while (method != null) {
            if ((statement.getAccessModifier() == ((MethodSymbol) method).getAccessModifier()) &&
                    (statement.isStatic() == ((MethodSymbol) method).isStatic()) &&
                    (methodType.isEqual(method.getType()))) {
                currentMethod = (MethodSymbol) method;
                break;
            }

            method = method.getNextSymbol();
        }

        mCurrentScope = currentMethod;

        statement.getBody().visit(this);

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
        return expression.getType();
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

        if ((typeLeftExpression.isEqual(typeRightExpression)) && ((typeLeftExpression.isInt() || (typeLeftExpression.isChar())))) {
            return Type.Bool;
        }

        printErrorMessage(expression.getToken(), "Operator '!=' only accepts operands of type int or char. Both operands must have the same type.");

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

            printErrorMessage(statement.getToken(), "Can not assign value of type '" + typeInitExpression + "' to variable of type '" + typeVariable + "'.");
        }

        return Type.Error;
    }

    @Override
    public BaseType visit(While statement) {
        return Type.Nothing;
    }

    private void printErrorMessage(Token token, String message) {
        System.out.println("[ " + token.getFileName() + " : " + token.getLine() + " ] " + message);
    }

}
