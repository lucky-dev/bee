package bee.lang.translate;

import bee.lang.ast.*;
import bee.lang.ir.Label;
import bee.lang.ir.Temp;
import bee.lang.ir.tree.*;
import bee.lang.visitors.IRTreeVisitor;

import java.util.Iterator;

public class NewIRTreeVisitor implements IRTreeVisitor {

    private Label mCurrentLblEnd;
    private Label mCurrentLblBeginLoop;

    @Override
    public WrapperIRExpression visit(Add expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.PLUS, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(And expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        Temp result = new Temp();
        Label lblTrue = Label.newLabel();
        Label lblFalse = Label.newLabel();
        Label lblEnd = Label.newLabel();

        return new Ex(
                new ESEQ(
                        new SEQ(
                                new MOVE(new TEMP(result), new CONST(1)),
                                new SEQ(
                                        new CJUMP(TypeRelOp.EQ, leftExpression.unEx(), new CONST(1), lblTrue, lblFalse),
                                        new SEQ(
                                                new LABEL(lblTrue),
                                                new SEQ(
                                                        new CJUMP(TypeRelOp.EQ, rightExpression.unEx(), new CONST(1), lblEnd, lblFalse),
                                                        new SEQ(
                                                                new LABEL(lblFalse),
                                                                new SEQ(
                                                                        new MOVE(new TEMP(result), new CONST(0)),
                                                                        new LABEL(lblEnd)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new TEMP(result)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(ArrayAccess expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(Assignment expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(AssignmentStatement statement) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(Block statement) {
        return statement.getStatements().visit(this);
    }

    @Override
    public WrapperIRExpression visit(BoolLiteral expression) {
        return new Ex(new CONST(expression.getValue() ? 1 : 0));
    }

    @Override
    public WrapperIRExpression visit(Break statement) {
        return new Nx(new JUMP(mCurrentLblEnd));
    }

    @Override
    public WrapperIRExpression visit(Continue statement) {
        return new Nx(new JUMP(mCurrentLblBeginLoop));
    }

    @Override
    public WrapperIRExpression visit(Call expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(CharLiteral expression) {
        return new Ex(new CONST(expression.getValue()));
    }

    @Override
    public WrapperIRExpression visit(ClassDefinition statement) {
        statement.getConstructorDefinitions().visit(this);
        statement.getMethodDefinitions().visit(this);

        return null;
    }

    @Override
    public WrapperIRExpression visit(ConstructorDefinition statement) {
        statement.getBody().visit(this);

        return null;
    }

    @Override
    public WrapperIRExpression visit(Div expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.DIV, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(DoWhile statement) {
        Label lblEnd = Label.newLabel();

        Label lblBeginLoop = Label.newLabel();

        Label savedLblEnd = mCurrentLblEnd;
        mCurrentLblEnd = lblEnd;

        Label saveLblBeginLoop = mCurrentLblBeginLoop;
        mCurrentLblBeginLoop = lblBeginLoop;

        WrapperIRExpression expression = statement.getExpression().visit(this);
        WrapperIRExpression body = statement.getStatement().visit(this);

        mCurrentLblEnd = savedLblEnd;

        mCurrentLblBeginLoop = saveLblBeginLoop;

        return new Nx(
                new SEQ(
                        new SEQ(
                                new SEQ(
                                        new LABEL(lblBeginLoop),
                                        body.unNx()
                                ),
                                new CJUMP(TypeRelOp.EQ, expression.unEx(), new CONST(1), lblBeginLoop, lblEnd)
                        ),
                        new LABEL(lblEnd)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(Equal expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.EQ, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(FieldAccess expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(FieldDefinition statement) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(GreaterEqualThan expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.GE, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(GreaterThan expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.GT, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(Identifier expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(If statement) {
        WrapperIRExpression expression = statement.getExpression().visit(this);
        WrapperIRExpression thenStatement = statement.getThenStatement().visit(this);

        Label lblTrue = Label.newLabel();
        Label lblFalse = Label.newLabel();

        if (statement.getElseStatement() == null) {
            return new Nx(
                    new SEQ(
                            new CJUMP(TypeRelOp.EQ, expression.unEx(), new CONST(1), lblTrue, lblFalse),
                            new SEQ(
                                    new LABEL(lblTrue),
                                    new SEQ(
                                            thenStatement.unNx(),
                                            new LABEL(lblFalse)
                                    )
                            )
                    )
            );
        } else {
            WrapperIRExpression elseStatement = statement.getElseStatement().visit(this);

            Label lblEnd = Label.newLabel();

            return new Nx(
                    new SEQ(
                            new CJUMP(TypeRelOp.EQ, expression.unEx(), new CONST(1), lblTrue, lblFalse),
                            new SEQ(
                                    new LABEL(lblTrue),
                                    new SEQ(
                                            thenStatement.unNx(),
                                            new SEQ(
                                                    new JUMP(lblEnd),
                                                    new SEQ(
                                                            new LABEL(lblFalse),
                                                            new SEQ(
                                                                    elseStatement.unNx(),
                                                                    new LABEL(lblEnd)
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
            );
        }
    }

    @Override
    public WrapperIRExpression visit(IntLiteral expression) {
        return new Ex(new CONST(expression.getValue()));
    }

    @Override
    public WrapperIRExpression visit(LessEqualThan expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.LE, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(LessThan expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.LT, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(MethodDefinition statement) {
        statement.getBody().visit(this);

        return null;
    }

    @Override
    public WrapperIRExpression visit(Mod expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.MOD, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(NewArray expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(NewObject expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(Nil expression) {
        return new Ex(new CONST(0));
    }

    @Override
    public WrapperIRExpression visit(Not expression) {
        WrapperIRExpression expr = expression.getExpression().visit(this);

        Temp result = new Temp();
        Label lblTrue = Label.newLabel();
        Label lblFalse = Label.newLabel();

        return new Ex(
                new ESEQ(
                        new SEQ(
                                new MOVE(new TEMP(result), new CONST(1)),
                                new SEQ(
                                        new SEQ(new CJUMP(TypeRelOp.EQ, expr.unEx(), new CONST(1), lblTrue, lblFalse),
                                                new SEQ(
                                                        new LABEL(lblTrue),
                                                        new MOVE(new TEMP(result), new CONST(0)))
                                        ),
                                        new LABEL(lblFalse)
                                )
                        ),
                new TEMP(result))
        );
    }

    @Override
    public WrapperIRExpression visit(NotEqual expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new RelCx(TypeRelOp.NE, leftExpression.unEx(), rightExpression.unEx());
    }

    @Override
    public WrapperIRExpression visit(Or expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        Temp result = new Temp();
        Label lblFalse = Label.newLabel();
        Label lblEnd = Label.newLabel();

        return new Ex(
                new ESEQ(
                        new SEQ(
                                new MOVE(new TEMP(result), new CONST(1)),
                                new SEQ(
                                        new CJUMP(TypeRelOp.EQ, leftExpression.unEx(), new CONST(1), lblEnd, lblFalse),
                                        new SEQ(
                                                new LABEL(lblFalse),
                                                new SEQ(
                                                        new CJUMP(TypeRelOp.EQ, rightExpression.unEx(), new CONST(1), lblEnd, lblFalse),
                                                        new SEQ(
                                                                new LABEL(lblFalse),
                                                                new SEQ(
                                                                        new MOVE(new TEMP(result), new CONST(0)),
                                                                        new LABEL(lblEnd)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new TEMP(result)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(Program statement) {
        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }

        return null;
    }

    @Override
    public WrapperIRExpression visit(Return statement) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(Statements statement) {
        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }

        return null;
    }

    @Override
    public WrapperIRExpression visit(StringLiteral expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(Subtract expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.MINUS, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(Super expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(TernaryOperator expression) {
        WrapperIRExpression conditionalExpression = expression.getConditionalExpression().visit(this);
        WrapperIRExpression thenExpression = expression.getThenExpression().visit(this);
        WrapperIRExpression elseExpression = expression.getElseExpression().visit(this);

        Temp result = new Temp();
        Label lblTrue = Label.newLabel();
        Label lblFalse = Label.newLabel();
        Label lblEnd = Label.newLabel();

        return new Ex(
                new ESEQ(
                        new SEQ(
                                new CJUMP(TypeRelOp.EQ, conditionalExpression.unEx(), new CONST(1), lblTrue, lblFalse),
                                new SEQ(
                                        new LABEL(lblTrue),
                                        new SEQ(
                                                new MOVE(new TEMP(result), thenExpression.unEx()),
                                                new SEQ(
                                                        new JUMP(lblEnd),
                                                        new SEQ(
                                                                new LABEL(lblFalse),
                                                                new SEQ(
                                                                        new MOVE(new TEMP(result), elseExpression.unEx()),
                                                                        new LABEL(lblEnd)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new TEMP(result)
                )
        );
    }

    @Override
    public WrapperIRExpression visit(This expression) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(Times expression) {
        WrapperIRExpression leftExpression = expression.getLeftExpression().visit(this);
        WrapperIRExpression rightExpression = expression.getRightExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.MUL, leftExpression.unEx(), rightExpression.unEx()));
    }

    @Override
    public WrapperIRExpression visit(UnaryMinus expression) {
        WrapperIRExpression expr = expression.getExpression().visit(this);

        return new Ex(new BINOP(TypeBinOp.MINUS, new CONST(0), expr.unEx()));
    }

    @Override
    public WrapperIRExpression visit(VariableDefinition statement) {
        return null;
    }

    @Override
    public WrapperIRExpression visit(While statement) {
        Label lblEnd = Label.newLabel();
        Label lblBeginLoop = Label.newLabel();

        Label savedLblEnd = mCurrentLblEnd;
        mCurrentLblEnd = lblEnd;

        Label saveLblBeginLoop = mCurrentLblBeginLoop;
        mCurrentLblBeginLoop = lblBeginLoop;

        WrapperIRExpression expression = statement.getExpression().visit(this);
        WrapperIRExpression body = statement.getStatement().visit(this);

        mCurrentLblEnd = savedLblEnd;

        mCurrentLblBeginLoop = saveLblBeginLoop;

        Label lblTrue = Label.newLabel();

        return new Nx(
                new SEQ(
                        new LABEL(lblBeginLoop),
                        new SEQ(
                                new CJUMP(TypeRelOp.EQ, expression.unEx(), new CONST(1), lblTrue, lblEnd),
                                new SEQ(
                                        new LABEL(lblTrue),
                                        new SEQ(
                                                body.unNx(),
                                                new SEQ(
                                                        new JUMP(lblBeginLoop),
                                                        new LABEL(lblEnd)
                                                )
                                        )
                                )
                        )
                )
        );
    }

}
