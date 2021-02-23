package bee.lang.semanalysis;

import bee.lang.ast.*;
import bee.lang.ast.types.*;
import bee.lang.symtable.BaseScope;
import bee.lang.symtable.ClassSymbol;
import bee.lang.symtable.MethodSymbol;
import bee.lang.symtable.Symbol;

import java.util.Iterator;

public class ValidatingConstructorsVisitor extends TypeCheckingVisitor {

    public ValidatingConstructorsVisitor(BaseScope baseScope) {
        super(baseScope);
    }

    @Override
    public BaseType visit(ConstructorDefinition statement) {
        // Bee does not the root class for all classes by default. If the keyword `super` uses in a constructor of a class without a base class then need to ignore this case and do not find constructors in a base class.
        if ((statement.getSuperConstructorArgumentsList() != null) && (!statement.getSuperConstructorArgumentsList().getExpressionList().isEmpty()) && (mCurrentClassSymbol.getBaseClassIdentifier() == null)) {
            printErrorMessage(statement.getToken(), "A class without a base class can not call super-constructor with arguments.");
            return Type.Error;
        }

        if ((statement.getSuperConstructorArgumentsList() != null) && (mCurrentClassSymbol.getBaseClassIdentifier() == null)) {
            return ((ClassClassType) mCurrentClassSymbol.getType()).getClassType();
        }

        MethodType expectedMethodType = new MethodType();

        Iterator<Expression> iterator = (statement.getSuperConstructorArgumentsList() != null ?
                statement.getSuperConstructorArgumentsList() : statement.getOtherConstructorArgumentsList()).getExpressionList().iterator();

        while (iterator.hasNext()) {
            expectedMethodType.addFormalArgumentType(iterator.next().visit(this));
        }

        expectedMethodType.addReturnType(((ClassClassType) mCurrentClassSymbol.getType()).getClassType());

        MethodSymbol foundMethodSymbol = null;

        BaseScope scope = statement.getSuperConstructorArgumentsList() != null ? mCurrentScope.getEnclosingScope() : mCurrentClassSymbol;

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

                    // If a method exists in base class and has access modifier `public` or `protected`. It can be used in subclasses. Or if the method exists in the current class then everything is OK.
                    if ((isSuitableMethod) && ((scope == mCurrentClassSymbol) || ((((MethodSymbol) symbol).isPublic()) || (((MethodSymbol) symbol).isProtected())))) {
                        // Found method may be inherited method or overridden. If method is inherited then need to check formal arguments to prevent clashing of methods.
                        // If method is overridden then everything is OK. Keep searching of other methods.
                        if (foundMethodSymbol == null) {
                            foundMethodSymbol = (MethodSymbol) symbol;
                        } else {
                            // Another method with the same formal parameters are found. It is an error.
                            if (!expectedMethodType.isEqualFormalArguments((MethodType) foundMethodSymbol.getType())) {
                                printErrorMessage(statement.getToken(), "Clash of constructors.");
                                return Type.Error;
                            }
                        }
                    }
                }

                symbol = symbol.getNextSymbol();
            }
        }

        if (foundMethodSymbol != null) {
            return ((MethodType) foundMethodSymbol.getType()).getReturnType();
        } else {
            printErrorMessage(statement.getToken(), "Can not find a constructor for such arguments.");
            return Type.Error;
        }
    }

}
