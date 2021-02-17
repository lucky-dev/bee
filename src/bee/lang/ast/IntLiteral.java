package bee.lang.ast;

import bee.lang.lexer.Token;
import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class IntLiteral extends Expression {

    private Token mToken;

    public IntLiteral(Token token) {
        mToken = token;
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
