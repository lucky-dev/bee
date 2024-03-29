package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.lexer.Token;
import bee.lang.symtable.Symbol;
import bee.lang.translate.ir.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class ConstructorDefinition extends Statement {

    private Token mToken;
    private AccessModifier mAccessModifier;
    private Statements mFormalArgumentsList;
    private ArgumentsList mSuperConstructorArgumentsList;
    private ArgumentsList mOtherConstructorArgumentsList;
    private Statements mBody;
    private Symbol mSymbol;
    private Symbol mOtherConstructorSymbol;

    public ConstructorDefinition(Token token, AccessModifier accessModifier, Statements formalArgumentsList, ArgumentsList superConstructorArgumentsList, ArgumentsList otherConstructorArgumentsList, Statements body) {
        mToken = token;
        mAccessModifier = accessModifier;
        mFormalArgumentsList = formalArgumentsList;
        mSuperConstructorArgumentsList = superConstructorArgumentsList;
        mOtherConstructorArgumentsList = otherConstructorArgumentsList;
        mBody = body;
    }

    public ArgumentsList getSuperConstructorArgumentsList() {
        return mSuperConstructorArgumentsList;
    }

    public ArgumentsList getOtherConstructorArgumentsList() {
        return mOtherConstructorArgumentsList;
    }

    public Token getToken() {
        return mToken;
    }

    public AccessModifier getAccessModifier() {
        return mAccessModifier;
    }

    public Statements getFormalArgumentsList() {
        return mFormalArgumentsList;
    }

    public Statements getBody() {
        return mBody;
    }

    public Symbol getSymbol() {
        return mSymbol;
    }

    public void setSymbol(Symbol symbol) {
        mSymbol = symbol;
    }

    public Symbol getOtherConstructorSymbol() {
        return mOtherConstructorSymbol;
    }

    public void setOtherConstructorSymbol(Symbol otherConstructorSymbol) {
        mOtherConstructorSymbol = otherConstructorSymbol;
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
