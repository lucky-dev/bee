package bee.lang.semanalysis;

import bee.lang.ast.*;
import bee.lang.exceptions.ValidatingLoopsException;
import bee.lang.lexer.Token;
import bee.lang.visitors.BaseVisitor;

import java.util.*;

public class ValidatingLoopsVisitor implements BaseVisitor {

    private int mCountLoops = 0;
    private boolean hasErrors;

    public ValidatingLoopsVisitor() {
        hasErrors = false;
    }

    public void validateLoops(Program program) throws ValidatingLoopsException {
        visit(program);

        if (hasErrors) {
            throw new ValidatingLoopsException();
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
        statement.getStatements().visit(this);
    }

    @Override
    public void visit(BoolLiteral expression) {
    }

    @Override
    public void visit(Break statement) {
        if (mCountLoops == 0) {
            printErrorMessage(statement.getToken(), "The keyword `break` is outside of a loop");
        }
    }

    @Override
    public void visit(Continue statement) {
        if (mCountLoops == 0) {
            printErrorMessage(statement.getToken(), "The keyword `continue` is outside of a loop");
        }
    }

    @Override
    public void visit(Call expression) {
    }

    @Override
    public void visit(CharLiteral expression) {
    }

    @Override
    public void visit(ClassDefinition statement) {
        statement.getConstructorDefinitions().visit(this);
        statement.getMethodDefinitions().visit(this);
    }

    @Override
    public void visit(ConstructorDefinition statement) {
        statement.getBody().visit(this);
    }

    @Override
    public void visit(Div expression) {
    }

    @Override
    public void visit(DoWhile statement) {
        mCountLoops++;
        statement.getStatement().visit(this);
        mCountLoops--;
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
        statement.getThenStatement().visit(this);
        if (statement.getElseStatement() != null) {
            statement.getElseStatement().visit(this);
        }
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
        statement.getBody().visit(this);
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
        expression.getThenExpression().visit(this);
        expression.getElseExpression().visit(this);
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
        mCountLoops++;
        statement.getStatement().visit(this);
        mCountLoops--;
    }

    private void printErrorMessage(Token token, String message) {
        hasErrors = true;
        System.out.println((token == null ? "" : "[ " + token.getFileName() + " : " + token.getLine() + " ] ") + message);
    }

}
