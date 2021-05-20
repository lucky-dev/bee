package bee.lang.visitors;

import bee.lang.ast.*;
import bee.lang.translate.ir.WrapperIRExpression;

public interface IRTreeVisitor {

    WrapperIRExpression visit(Add expression);
    WrapperIRExpression visit(And expression);
    WrapperIRExpression visit(ArrayAccess expression);
    WrapperIRExpression visit(Assignment expression);
    WrapperIRExpression visit(AssignmentStatement statement);
    WrapperIRExpression visit(Block statement);
    WrapperIRExpression visit(BoolLiteral expression);
    WrapperIRExpression visit(Break statement);
    WrapperIRExpression visit(Continue statement);
    WrapperIRExpression visit(Call expression);
    WrapperIRExpression visit(ExternalFunctionDeclaration statement);
    WrapperIRExpression visit(ExternalCall expression);
    WrapperIRExpression visit(CharLiteral expression);
    WrapperIRExpression visit(ClassDefinition statement);
    WrapperIRExpression visit(ConstructorDefinition statement);
    WrapperIRExpression visit(Div expression);
    WrapperIRExpression visit(DoWhile statement);
    WrapperIRExpression visit(Equal expression);
    WrapperIRExpression visit(FieldAccess expression);
    WrapperIRExpression visit(FieldDefinition statement);
    WrapperIRExpression visit(GreaterEqualThan expression);
    WrapperIRExpression visit(GreaterThan expression);
    WrapperIRExpression visit(Identifier expression);
    WrapperIRExpression visit(If statement);
    WrapperIRExpression visit(IntLiteral expression);
    WrapperIRExpression visit(LessEqualThan expression);
    WrapperIRExpression visit(LessThan expression);
    WrapperIRExpression visit(MethodDefinition statement);
    WrapperIRExpression visit(Mod expression);
    WrapperIRExpression visit(NewArray expression);
    WrapperIRExpression visit(NewObject expression);
    WrapperIRExpression visit(Nil expression);
    WrapperIRExpression visit(Not expression);
    WrapperIRExpression visit(NotEqual expression);
    WrapperIRExpression visit(Or expression);
    WrapperIRExpression visit(Program statement);
    WrapperIRExpression visit(Return statement);
    WrapperIRExpression visit(Statements statement);
    WrapperIRExpression visit(StringLiteral expression);
    WrapperIRExpression visit(Subtract expression);
    WrapperIRExpression visit(Super expression);
    WrapperIRExpression visit(TernaryOperator expression);
    WrapperIRExpression visit(This expression);
    WrapperIRExpression visit(Times expression);
    WrapperIRExpression visit(UnaryMinus expression);
    WrapperIRExpression visit(VariableDefinition statement);
    WrapperIRExpression visit(While statement);
    
}
