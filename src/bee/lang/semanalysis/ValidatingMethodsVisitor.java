package bee.lang.semanalysis;

import bee.lang.ast.*;
import bee.lang.ast.types.MethodType;
import bee.lang.ast.types.Type;
import bee.lang.lexer.Token;
import bee.lang.symtable.*;
import bee.lang.visitors.BaseVisitor;

import java.util.*;

public class ValidatingMethodsVisitor implements BaseVisitor {

    private BaseScope mCurrentScope;

    public ValidatingMethodsVisitor(BaseScope scope) {
        mCurrentScope = scope;
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
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visit(AssignmentStatement statement) {
        statement.getExpression().visit(this);
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
    public void visit(CharLiteral expression) {
    }

    @Override
    public void visit(ClassDefinition statement) {
        ClassSymbol classSymbol = (ClassSymbol) mCurrentScope.getSymbolInCurrentScope(statement.getClassIdentifier().getName());

        mCurrentScope = classSymbol;

        statement.getFieldDefinitions().visit(this);
        statement.getConstructorDefinitions().visit(this);
        statement.getMethodDefinitions().visit(this);

        mCurrentScope = classSymbol.getScope();
    }

    @Override
    public void visit(ConstructorDefinition statement) {
        // Create a new method type
        MethodType constructorType = new MethodType();

        Iterator<Statement> iterator = statement.getFormalArgumentsList().getStatementsList().iterator();

        while (iterator.hasNext()) {
            VariableDefinition variableDefinition = (VariableDefinition) iterator.next();
            constructorType.addFormalArgumentType(variableDefinition.getType());
        }

        constructorType.addReturnType(Type.Class(((ClassSymbol) mCurrentScope).getIdentifier()));

        // Find constructor (method) in the current class
        Symbol symbol = mCurrentScope.getSymbolInCurrentScope("constructor");

        Symbol currentConstructor = null;

        Symbol constructor = symbol;

        // The symbol table was created at the previous stage. Find a constructor which corresponds to the visited constructor.
        while (constructor != null) {
            if ((statement.getAccessModifier() == ((MethodSymbol) constructor).getAccessModifier()) &&
                    (constructorType.isEqual(constructor.getType()))) {
                currentConstructor = constructor;
                break;
            }

            constructor = constructor.getNextSymbol();
        }

        constructor = symbol;

        // Try to find a constructor with the same signature in the current class. Ignore the current constructor.
        while (constructor != null) {
            if (constructor != currentConstructor) {
                if (currentConstructor.getType().isEqual(constructor.getType())) {
                    printErrorMessage(statement.getToken(), "Class '" + mCurrentScope.getScopeName() + "' has already had a constructor with the same arguments");
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
        // Create a new method type.
        MethodType methodType = new MethodType();

        Iterator<Statement> iterator = statement.getFormalArgumentsList().getStatementsList().iterator();

        while (iterator.hasNext()) {
            methodType.addFormalArgumentType(((VariableDefinition) iterator.next()).getType());
        }

        methodType.addReturnType(statement.getReturnType());

        Symbol symbol = mCurrentScope.getSymbolInCurrentScope(statement.getIdentifier().getName());

        MethodSymbol currentMethod = null;

        // Try to find a method which corresponds to the visited method.
        while (symbol != null) {
            if ((statement.getAccessModifier() == ((MethodSymbol) symbol).getAccessModifier()) &&
                    ((statement.isStatic() == ((MethodSymbol) symbol).isStatic())) &&
                    (methodType.isEqual(symbol.getType()))) {
                currentMethod = (MethodSymbol) symbol;
                break;
            }

            symbol = symbol.getNextSymbol();
        }

        BaseScope scope = mCurrentScope;

        // Scan all classes (scopes) in hierarchy.
        while (scope != null) {
            symbol = scope.getSymbolInCurrentScope(statement.getIdentifier().getName());

            // Ignore fields with the same name.
            if (symbol instanceof MethodSymbol) {
                MethodSymbol foundMethodSymbol = null;

                Symbol currentSymbol = symbol;

                // Try to find a method in a class. Check only formal arguments. Ignore return type.
                while (currentSymbol != null) {
                    // Skip the current method
                    if ((currentMethod != symbol) &&
                            (((MethodSymbol) symbol).isStatic() == statement.isStatic()) &&
                            (((MethodType) currentMethod.getType()).isEqualFormalArguments((MethodType) currentSymbol.getType()))) {
                        foundMethodSymbol = (MethodSymbol) currentSymbol;
                        break;
                    }

                    currentSymbol = currentSymbol.getNextSymbol();
                }

                if (foundMethodSymbol != null) {
                    // Block a method with the same name, the same static modifier and the same formal arguments in the current class.
                    if (scope == mCurrentScope) {
                        printErrorMessage(statement.getIdentifier().getToken(), "'" + mCurrentScope.getScopeName() + "' has the method '" + statement.getIdentifier().getName() + "' with the same signature.");
                        break;
                    } else {
                        if (((MethodType) currentMethod.getType()).getReturnType().isEqual(((MethodType) foundMethodSymbol.getType()).getReturnType())) {
                            // Methods have the same formal parameters and return types. These methods are overridden (or hides static methods).
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
        System.out.println((token == null ? "" : "[ " + token.getFileName() + " : " + token.getLine() + " ] ") + message);
    }

}
