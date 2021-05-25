package bee.lang.visitors;

import bee.lang.ast.*;

public interface BaseVisitor {

    void visit(Add expression);
    void visit(And expression);
    void visit(ArrayAccess expression);
    void visit(Assignment expression);
    void visit(AssignmentStatement statement);
    void visit(Block statement);
    void visit(BoolLiteral expression);
    void visit(Break statement);
    void visit(Continue statement);
    void visit(Call expression);
    void visit(ExternalFunctionDeclaration statement);
    void visit(ExternalCall expression);
    void visit(CharLiteral expression);
    void visit(ClassDefinition statement);
    void visit(ConstructorDefinition statement);
    void visit(Div expression);
    void visit(DoWhile statement);
    void visit(Equal expression);
    void visit(FieldAccess expression);
    void visit(FieldDefinition statement);
    void visit(GreaterEqualThan expression);
    void visit(GreaterThan expression);
    void visit(Identifier expression);
    void visit(If statement);
    void visit(IntLiteral expression);
    void visit(LessEqualThan expression);
    void visit(LessThan expression);
    void visit(MethodDefinition statement);
    void visit(Mod expression);
    void visit(NewArray expression);
    void visit(NewObject expression);
    void visit(Nil expression);
    void visit(Not expression);
    void visit(NotEqual expression);
    void visit(Or expression);
    void visit(Program statement);
    void visit(Return statement);
    void visit(Statements statement);
    void visit(StringLiteral expression);
    void visit(Subtract expression);
    void visit(Super expression);
    void visit(TernaryOperator expression);
    void visit(This expression);
    void visit(Times expression);
    void visit(UnaryMinus expression);
    void visit(VariableDefinition statement);
    void visit(While statement);

}
