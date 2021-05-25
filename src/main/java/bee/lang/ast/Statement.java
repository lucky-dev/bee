package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.symtable.BaseScope;
import bee.lang.translate.ir.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public abstract class Statement {

    private BaseScope mScope;

    public BaseScope getScope() {
        return mScope;
    }

    public void setScope(BaseScope scope) {
        mScope = scope;
    }

    abstract public BaseType visit(TypeVisitor visitor);
    abstract public void visit(BaseVisitor visitor);
    abstract public WrapperIRExpression visit(IRTreeVisitor visitor);

}
