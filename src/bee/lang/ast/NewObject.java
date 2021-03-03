package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.lexer.Token;
import bee.lang.translate.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class NewObject extends Expression {

    private Token mToken;
    private BaseType mType;
    private ArgumentsList mArgumentsList;

    public NewObject(Token token, BaseType type, ArgumentsList argumentsList) {
        mToken = token;
        mType = type;
        mArgumentsList = argumentsList;
    }

    public Token getToken() {
        return mToken;
    }

    public BaseType getType() {
        return mType;
    }

    public ArgumentsList getArgumentsList() {
        return mArgumentsList;
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
