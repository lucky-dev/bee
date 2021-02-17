package bee.lang.semanalysis;

import bee.lang.ast.*;
import bee.lang.ast.types.MethodType;
import bee.lang.ast.types.Type;
import bee.lang.lexer.Token;
import bee.lang.symtable.*;
import bee.lang.visitors.BaseVisitor;

import java.util.*;

public class ValidatingMethodsSymbolTableVisitor implements BaseVisitor {

    private BaseScope mCurrentScope;

    public ValidatingMethodsSymbolTableVisitor(BaseScope scope) {
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
    public void visit(ChainingCall expression) {
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
        MethodType constructorType = new MethodType();

        Iterator<Statement> iterator = statement.getFormalArgumentsList().getStatementsList().iterator();

        while (iterator.hasNext()) {
            VariableDefinition variableDefinition = (VariableDefinition) iterator.next();
            constructorType.addFormalArgumentType(variableDefinition.getType());
        }

        constructorType.addReturnType(Type.Class(((ClassSymbol) mCurrentScope).getIdentifier()));

        Symbol symbol = mCurrentScope.getSymbolInCurrentScope("constructor");

        Symbol currentConstructor = null;

        Symbol constructor = symbol;

        while (constructor != null) {
            if ((statement.getAccessModifier() == ((MethodSymbol) constructor).getAccessModifier()) &&
                    (constructorType.isEqual(constructor.getType()))) {
                currentConstructor = constructor;
                break;
            }

            constructor = constructor.getNextSymbol();
        }

        constructor = symbol;

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
        MethodType methodType = new MethodType();

        Iterator<Statement> iterator = statement.getFormalArgumentsList().getStatementsList().iterator();

        while (iterator.hasNext()) {
            methodType.addFormalArgumentType(((VariableDefinition) iterator.next()).getType());
        }

        methodType.addReturnType(statement.getReturnType());

        Symbol symbol = mCurrentScope.getSymbolInCurrentScope(statement.getIdentifier().getName());

        MethodSymbol currentMethod = null;

        Symbol method = symbol;

        while (method != null) {
            if ((statement.getAccessModifier() == ((MethodSymbol) method).getAccessModifier()) &&
                    ((statement.isStatic() == ((MethodSymbol) method).isStatic())) &&
                    (methodType.isEqual(method.getType()))) {
                currentMethod = (MethodSymbol) method;
                break;
            }

            method = method.getNextSymbol();
        }

        BaseScope scope = mCurrentScope;

        while (scope != null) {
            symbol = scope.getSymbolInCurrentScope(statement.getIdentifier().getName());

            if ((symbol != null) && (symbol.getSymbolType() == SymbolType.METHOD)) {
                MethodSymbol foundMethodSymbol = null;

                Symbol currentSymbol = symbol;

                while (currentSymbol != null) {
                    if (currentMethod != symbol) {
                        if (((MethodType) currentMethod.getType()).isEqualFormalArguments((MethodType) currentSymbol.getType())) {
                            foundMethodSymbol = (MethodSymbol) currentSymbol;
                            break;
                        }
                    }

                    currentSymbol = currentSymbol.getNextSymbol();
                }

                if (foundMethodSymbol != null) {
                    if (scope == mCurrentScope) {
                        printErrorMessage(statement.getIdentifier().getToken(), "'" + mCurrentScope.getScopeName() + "' has the method '" + statement.getIdentifier().getName() + "' with the same signature");
                        break;
                    } else {
                        if (((currentMethod.isStatic()) && (foundMethodSymbol.isStatic())) && ((foundMethodSymbol.isPublic()) || (foundMethodSymbol.isProtected()))) {
                            printErrorMessage(statement.getIdentifier().getToken(), "'" + scope + "' has already had the name '" + statement.getIdentifier().getName() + "'");
                            break;
                        }

                        if ((!currentMethod.isStatic()) && (!foundMethodSymbol.isStatic())) {
                            if (((MethodType) currentMethod.getType()).getReturnType().isEqual(((MethodType) foundMethodSymbol.getType()).getReturnType())) {
                                if (currentMethod.getAccessModifier().isWeakerThan(foundMethodSymbol.getAccessModifier())) {
                                    printErrorMessage(statement.getIdentifier().getToken(), "'" + scope + "' has already had the name '" + statement.getIdentifier().getName() + "' with wider access modifier");
                                    break;
                                }
                            } else {
                                if ((foundMethodSymbol.isPublic()) || (foundMethodSymbol.isProtected())) {
                                    printErrorMessage(statement.getIdentifier().getToken(), "'" + scope + "' has already had the name '" + statement.getIdentifier().getName() + "'");
                                    break;
                                }
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
