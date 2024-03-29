package bee.lang.semanalysis;

import bee.lang.ast.*;
import bee.lang.ast.types.MethodType;
import bee.lang.exceptions.ValidatingMethodsException;
import bee.lang.lexer.Token;
import bee.lang.symtable.BaseScope;
import bee.lang.symtable.ClassSymbol;
import bee.lang.symtable.MethodSymbol;
import bee.lang.symtable.Symbol;
import bee.lang.visitors.BaseVisitor;

import java.util.Iterator;

public class ValidatingMethodsVisitor implements BaseVisitor {

    private final static String ENTRY_POINT_METHOD = "main";

    private BaseScope mCurrentScope;
    private MethodSymbol mEntryPointMethod;
    private boolean hasErrors;

    public ValidatingMethodsVisitor(BaseScope scope) {
        mCurrentScope = scope;
        hasErrors = false;
    }

    public MethodSymbol getEntryPointMethod() {
        return mEntryPointMethod;
    }

    public void validateMethods(Program program) throws ValidatingMethodsException {
        visit(program);

        if (mEntryPointMethod == null) {
            throw new ValidatingMethodsException("No entry point. Need to add the method 'main' to some class.");
        }

        if (hasErrors) {
            throw new ValidatingMethodsException();
        }
    }

    @Override
    public void visit(Add expression) {
    }

    @Override
    public void visit(And expression) {
    }

    @Override
    public void visit(ArrayAccess expression) {
    }

    @Override
    public void visit(Assignment expression) {
    }

    @Override
    public void visit(AssignmentStatement statement) {
    }

    @Override
    public void visit(Block statement) {
    }

    @Override
    public void visit(BoolLiteral expression) {
    }

    @Override
    public void visit(Break statement) {
    }

    @Override
    public void visit(Continue statement) {
    }

    @Override
    public void visit(Call expression) {
    }

    @Override
    public void visit(ExternalFunctionDeclaration statement) {
        Symbol currentExternalFunction = statement.getSymbol();

        Symbol externalFunction = mCurrentScope.getSymbolInCurrentScope(statement.getIdentifier().getName());

        // Try to find an external function with the same signature in the current class. Ignore the current external function.
        while (externalFunction != null) {
            if (externalFunction != currentExternalFunction) {
                if (currentExternalFunction.getType().isEqual(externalFunction.getType())) {
                    printErrorMessage(statement.getIdentifier().getToken(), "Class '" + mCurrentScope.getScopeName() + "' has already had an external function with the same arguments.");
                    break;
                }
            }

            externalFunction = externalFunction.getNextSymbol();
        }
    }

    @Override
    public void visit(ExternalCall expression) {
    }

    @Override
    public void visit(CharLiteral expression) {
    }

    @Override
    public void visit(ClassDefinition statement) {
        ClassSymbol classSymbol = (ClassSymbol) mCurrentScope.getSymbolInCurrentScope(statement.getClassIdentifier().getName());

        mCurrentScope = classSymbol;

        statement.getConstructorDefinitions().visit(this);
        statement.getMethodDefinitions().visit(this);
        statement.getExternalFunctionDeclarations().visit(this);

        mCurrentScope = classSymbol.getScope();
    }

    @Override
    public void visit(ConstructorDefinition statement) {
        Symbol currentConstructor = statement.getSymbol();

        Symbol constructor = mCurrentScope.getSymbolInCurrentScope("constructor");

        // Try to find a constructor with the same signature in the current class. Ignore the current constructor.
        while (constructor != null) {
            if (constructor != currentConstructor) {
                if (currentConstructor.getType().isEqual(constructor.getType())) {
                    printErrorMessage(statement.getToken(), "Class '" + mCurrentScope.getScopeName() + "' has already had a constructor with the same arguments.");
                    break;
                }
            }

            constructor = constructor.getNextSymbol();
        }
    }

    @Override
    public void visit(Div expression) {
    }

    @Override
    public void visit(DoWhile statement) {
    }

    @Override
    public void visit(Equal expression) {
    }

    @Override
    public void visit(FieldAccess expression) {
    }

    @Override
    public void visit(FieldDefinition statement) {
    }

    @Override
    public void visit(GreaterEqualThan expression) {
    }

    @Override
    public void visit(GreaterThan expression) {
    }

    @Override
    public void visit(Identifier expression) {
    }

    @Override
    public void visit(If statement) {
    }

    @Override
    public void visit(IntLiteral expression) {
    }

    @Override
    public void visit(LessEqualThan expression) {
    }

    @Override
    public void visit(LessThan expression) {
    }

    @Override
    public void visit(MethodDefinition statement) {
        MethodSymbol currentMethod = (MethodSymbol) statement.getSymbol();

        BaseScope scope = mCurrentScope;

        Symbol symbol;

        // Scan all classes (scopes) in hierarchy.
        while (scope != null) {
            symbol = scope.getSymbolInCurrentScope(statement.getIdentifier().getName());

            if (symbol instanceof MethodSymbol) {
                MethodSymbol foundMethodSymbol = null;

                // Try to find a method in a class. Check only formal arguments. Ignore return type.
                while (symbol != null) {
                    // Skip the current method
                    if ((currentMethod != symbol) &&
                            (((MethodSymbol) symbol).isStatic() == statement.isStatic()) &&
                            (((MethodType) currentMethod.getType()).isEqualFormalArguments((MethodType) symbol.getType()))) {
                        foundMethodSymbol = (MethodSymbol) symbol;
                        break;
                    }

                    symbol = symbol.getNextSymbol();
                }

                if (foundMethodSymbol != null) {
                    // Block a method with the same name, the same static modifier and the same formal arguments in the current class.
                    if (scope == mCurrentScope) {
                        printErrorMessage(statement.getIdentifier().getToken(), "'" + mCurrentScope.getScopeName() + "' has the method '" + statement.getIdentifier().getName() + "' with the same signature.");
                        break;
                    } else {
                        if (((MethodType) currentMethod.getType()).getReturnType().isEqual(((MethodType) foundMethodSymbol.getType()).getReturnType())) {
                            // Methods have the same formal parameters and return types. These methods are overridden (or hide static methods).
                            // If the method in the subclass has weaker access modifier than the method in the base class. It is an error.
                            // E.g. Animal :> Cat. Animal#doIt (Animal::doIt) is public and Cat#doIt (Cat::doIt) is private or protected - error!
                            if (currentMethod.getAccessModifier().isWeakerThan(foundMethodSymbol.getAccessModifier())) {
                                printErrorMessage(statement.getIdentifier().getToken(), "'" + scope + "' has already had the name '" + statement.getIdentifier().getName() + "' with wider access modifier.");
                                break;
                            }
                        } else {
                            // Methods have the same formal parameters and different return types.
                            // This is a conflict. The method in the base class must be private to cancel inheritance.
                            if ((foundMethodSymbol.isPublic()) || (foundMethodSymbol.isProtected())) {
                                printErrorMessage(statement.getIdentifier().getToken(), "'" + scope + "' has already had the name '" + statement.getIdentifier().getName() + "'.");
                                break;
                            }
                        }
                    }
                }
            }

            scope = scope.getEnclosingScope();
        }

        if ((currentMethod.isStatic()) &&
                (currentMethod.getIdentifier().getName().equals(ENTRY_POINT_METHOD)) &&
                (((MethodType) currentMethod.getType()).getFormalArgumentTypes().isEmpty()) &&
                (((MethodType) currentMethod.getType()).getReturnType().isVoid())) {
            if (mEntryPointMethod == null) {
                mEntryPointMethod = currentMethod;
            } else {
                printErrorMessage(statement.getIdentifier().getToken(), "'" + mCurrentScope + "' can not have the method 'main' because it is defined in the class '" + mEntryPointMethod.getEnclosingScope().getScopeName() + "'.");
            }
        }
    }

    @Override
    public void visit(Mod expression) {
    }

    @Override
    public void visit(NewArray expression) {
    }

    @Override
    public void visit(NewObject expression) {
    }

    @Override
    public void visit(Nil expression) {
    }

    @Override
    public void visit(Not expression) {
    }

    @Override
    public void visit(NotEqual expression) {
    }

    @Override
    public void visit(Or expression) {
    }

    @Override
    public void visit(Program statement) {
        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }
    }

    @Override
    public void visit(Return statement) {
    }

    @Override
    public void visit(Statements statement) {
        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }
    }

    @Override
    public void visit(StringLiteral expression) {
    }

    @Override
    public void visit(Subtract expression) {
    }

    @Override
    public void visit(Super expression) {
    }

    @Override
    public void visit(TernaryOperator expression) {
    }

    @Override
    public void visit(This expression) {
    }

    @Override
    public void visit(Times expression) {
    }

    @Override
    public void visit(UnaryMinus expression) {
    }

    @Override
    public void visit(VariableDefinition statement) {
    }

    @Override
    public void visit(While statement) {
    }

    private void printErrorMessage(Token token, String message) {
        hasErrors = true;
        System.out.println((token == null ? "" : "[ " + token.getFileName() + " : " + token.getLine() + " ] ") + message);
    }

}
