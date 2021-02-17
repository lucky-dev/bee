package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class ChainingCall extends Call {

    private Expression mExpression;

    public ChainingCall(Expression expression, Identifier identifier, ArgumentsList argumentsList) {
        super(identifier, argumentsList);
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

}
