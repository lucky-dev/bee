package bee.lang.semanalysis;

import bee.lang.ast.*;
import bee.lang.ast.types.*;
import bee.lang.lexer.Token;
import bee.lang.symtable.BaseScope;
import bee.lang.symtable.ClassSymbol;
import bee.lang.symtable.MethodSymbol;
import bee.lang.symtable.Symbol;
import bee.lang.visitors.TypeVisitor;

import java.util.Iterator;

public class TypeCheckingVisitor implements TypeVisitor {

    private BaseScope mBaseScope;
    private BaseScope mCurrentScope;

    public TypeCheckingVisitor(BaseScope baseScope) {
        mBaseScope = baseScope;
        mCurrentScope = baseScope;
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
            printErrorMessage(expression.getToken(), "Operator '[]' can be applied to array");
        }

        if (!indexType.isInt()) {
            printErrorMessage(expression.getToken(), "Operator '[]' can be used with an index of type int");
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

        printErrorMessage(expression.getToken(), "Can not assign value of type '" + typeLeftExpression + "' to variable of type '" + typeRightExpression + "'");

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
        BaseType identifierType = expression.getIdentifier().visit(this);

        if (identifierType.isMethod()) {
        }

        return Type.Error;
    }

    @Override
    public BaseType visit(ChainingCall expression) {
        return null;
    }

    @Override
    public BaseType visit(CharLiteral expression) {
        return Type.Char;
    }

    @Override
    public BaseType visit(ClassDefinition statement) {
        ClassSymbol classSymbol = (ClassSymbol) mBaseScope.getSymbolInCurrentScope(statement.getClassIdentifier().getName());

        mCurrentScope = classSymbol;

        statement.getConstructorDefinitions().visit(this);
        statement.getMethodDefinitions().visit(this);
        statement.getFieldDefinitions().visit(this);

        mCurrentScope = classSymbol.getScope();

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
        return null;
    }

    @Override
    public BaseType visit(FieldDefinition statement) {
        return Type.Nothing;
    }

    @Override
    public BaseType visit(GreaterEqualThan expression) {
        return null;
    }

    @Override
    public BaseType visit(GreaterThan expression) {
        return null;
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
        return null;
    }

    @Override
    public BaseType visit(LessThan expression) {
        return null;
    }

    @Override
    public BaseType visit(MethodDefinition statement) {
        MethodType constructorType = new MethodType();

        Iterator<Statement> iterator = statement.getFormalArgumentsList().getStatementsList().iterator();

        while (iterator.hasNext()) {
            VariableDefinition variableDefinition = (VariableDefinition) iterator.next();
            constructorType.addFormalArgumentType(variableDefinition.getType());
        }

        constructorType.addReturnType(Type.Class(((ClassSymbol) mCurrentScope).getIdentifier()));

        Symbol symbol = mCurrentScope.getSymbolInCurrentScope(statement.getIdentifier().getName());

        MethodSymbol currentMethod = null;

        Symbol method = symbol;

        while (method != null) {
            if ((statement.getAccessModifier() == ((MethodSymbol) method).getAccessModifier()) &&
                    (statement.isStatic() == ((MethodSymbol) method).isStatic()) &&
                    (constructorType.isEqual(method.getType()))) {
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
        return null;
    }

    @Override
    public BaseType visit(NewArray expression) {
        return null;
    }

    @Override
    public BaseType visit(NewObject expression) {
        return null;
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
        return null;
    }

    @Override
    public BaseType visit(Or expression) {
        return null;
    }

    @Override
    public BaseType visit(Program statement) {
        return Type.Nothing;
    }

    @Override
    public BaseType visit(Return statement) {
        return Type.Nothing;
    }

    @Override
    public BaseType visit(Statements statement) {
        return Type.Nothing;
    }

    @Override
    public BaseType visit(StringLiteral expression) {
        return new ArrayType(Type.Char);
    }

    @Override
    public BaseType visit(Subtract expression) {
        return null;
    }

    @Override
    public BaseType visit(Super expression) {
        return null;
    }

    @Override
    public BaseType visit(TernaryOperator expression) {
        return null;
    }

    @Override
    public BaseType visit(This expression) {
        return null;
    }

    @Override
    public BaseType visit(Times expression) {
        return null;
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
        return Type.Nothing;
    }

    @Override
    public BaseType visit(While statement) {
        return Type.Nothing;
    }

    private void printErrorMessage(Token token, String message) {
        System.out.println("[ " + token.getFileName() + " : " + token.getLine() + " ] " + message);
    }

}
