package bee.lang.ast;

import bee.lang.ast.types.BaseType;
import bee.lang.visitors.BaseVisitor;
import bee.lang.visitors.TypeVisitor;

public class Program extends Statements {

    public void addClassDefinition(Statement classDefinition) {
        addStatement(classDefinition);
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