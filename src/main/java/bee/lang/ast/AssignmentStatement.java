package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.translate.ir.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class AssignmentStatement extends Statement {

    private Expression mExpression;

    public AssignmentStatement(Expression expression) {
        mExpression = expression;
    }

    public Expression getExpression() {
        return mExpression;
    }

    @Override
    public BaseType visit(TypeVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public void visit(BaseVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public WrapperIRExpression visit(IRTreeVisitor visitor) {
        return visitor.visit(this);
    }

}
