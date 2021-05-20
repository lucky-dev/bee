package bee.lang.visitors;

import bee.lang.ast.*;
import bee.lang.ast.types.BaseType;
import bee.lang.translate.ir.WrapperIRExpression;

public interface TypeVisitor {

    BaseType visit(Add expression);
    BaseType visit(And expression);
    BaseType visit(ArrayAccess expression);
    BaseType visit(Assignment expression);
    BaseType visit(AssignmentStatement statement);
    BaseType visit(Block statement);
    BaseType visit(BoolLiteral expression);
    BaseType visit(Break statement);
    BaseType visit(Continue statement);
    BaseType visit(Call expression);
    BaseType visit(ExternalFunctionDeclaration statement);
    BaseType visit(ExternalCall expression);
    BaseType visit(CharLiteral expression);
    BaseType visit(ClassDefinition statement);
    BaseType visit(ConstructorDefinition statement);
    BaseType visit(Div expression);
    BaseType visit(DoWhile statement);
    BaseType visit(Equal expression);
    BaseType visit(FieldAccess expression);
    BaseType visit(FieldDefinition statement);
    BaseType visit(GreaterEqualThan expression);
    BaseType visit(GreaterThan expression);
    BaseType visit(Identifier expression);
    BaseType visit(If statement);
    BaseType visit(IntLiteral expression);
    BaseType visit(LessEqualThan expression);
    BaseType visit(LessThan expression);
    BaseType visit(MethodDefinition statement);
    BaseType visit(Mod expression);
    BaseType visit(NewArray expression);
    BaseType visit(NewObject expression);
    BaseType visit(Nil expression);
    BaseType visit(Not expression);
    BaseType visit(NotEqual expression);
    BaseType visit(Or expression);
    BaseType visit(Program statement);
    BaseType visit(Return statement);
    BaseType visit(Statements statement);
    BaseType visit(StringLiteral expression);
    BaseType visit(Subtract expression);
    BaseType visit(Super expression);
    BaseType visit(TernaryOperator expression);
    BaseType visit(This expression);
    BaseType visit(Times expression);
    BaseType visit(UnaryMinus expression);
    BaseType visit(VariableDefinition statement);
    BaseType visit(While statement);

}
