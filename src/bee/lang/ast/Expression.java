package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.translate.WrapperIRExpression;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.IRTreeVisitor;
import bee.lang.visitors.TypeVisitor;

public abstract class Expression {

    abstract public BaseType visit(TypeVisitor visitor);
    abstract public void visit(BaseVisitor visitor);
    abstract public WrapperIRExpression visit(IRTreeVisitor visitor);

}
