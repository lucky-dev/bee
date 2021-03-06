package bee.lang.ast;

import bee.lang.lexer.Token;
import bee.lang.ast.types.BaseType;
import bee.lang.symtable.Symbol;
import bee.lang.translate.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class Identifier extends Expression {

    private Token mToken;
    private Symbol mSymbol;

    public Identifier(Token token) {
        mToken = token;
    }

    public Token getToken() {
        return mToken;
    }

    public String getName() {
        return mToken.getValue();
    }

    public Symbol getSymbol() {
        return mSymbol;
    }

    public void setSymbol(Symbol symbol) {
        mSymbol = symbol;
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
