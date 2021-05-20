package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.symtable.Symbol;
import bee.lang.translate.ir.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public class ClassDefinition extends Statement {

    private Identifier mClassIdentifier;
    private Identifier mBaseClassIdentifier;
    private Statements mConstructorDefinitions;
    private Statements mMethodDefinitions;
    private Statements mExternalFunctionDeclarations;
    private Statements mFieldDefinitions;
    private Symbol mSymbol;

    public ClassDefinition(Identifier baseClassIdentifier, Identifier classIdentifier, Statements constructorDefinitions, Statements methodDefinitions, Statements fieldDefinitions, Statements externalFunctionDeclarations) {
        mBaseClassIdentifier = baseClassIdentifier;
        mClassIdentifier = classIdentifier;
        mConstructorDefinitions = constructorDefinitions;
        mMethodDefinitions = methodDefinitions;
        mFieldDefinitions = fieldDefinitions;
        mExternalFunctionDeclarations = externalFunctionDeclarations;
    }

    public Identifier getClassIdentifier() {
        return mClassIdentifier;
    }

    public Identifier getBaseClassIdentifier() {
        return mBaseClassIdentifier;
    }

    public Statements getConstructorDefinitions() {
        return mConstructorDefinitions;
    }

    public Statements getMethodDefinitions() {
        return mMethodDefinitions;
    }

    public Statements getFieldDefinitions() {
        return mFieldDefinitions;
    }

    public Statements getExternalFunctionDeclarations() {
        return mExternalFunctionDeclarations;
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
