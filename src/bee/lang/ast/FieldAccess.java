package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.symtable.Symbol;
import bee.lang.translate.ir.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class FieldAccess extends Expression {

    private Expression mExpression;
    private Identifier mIdentifier;
    private Symbol mSymbol;

    public FieldAccess(Expression expression, Identifier identifier) {
        mExpression = expression;
        mIdentifier = identifier;
    }

    public Expression getExpression() {
        return mExpression;
    }

    public Identifier getIdentifier() {
        return mIdentifier;
    }

    public void setSymbol(Symbol symbol) {
        mSymbol = symbol;
    }

    public Symbol getSymbol() {
        return mSymbol;
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
