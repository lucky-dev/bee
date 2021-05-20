package bee.lang.translate;

import bee.lang.ast.*;
import bee.lang.ast.types.MethodType;
import bee.lang.symtable.*;
import bee.lang.visitors.BaseVisitor;

import java.util.*;

// This class collects information which will be used in the future to generate vtable (virtual table) for classes and represent fields of objects and classes in the memory.
public class NewLayoutsVisitor implements BaseVisitor {

    private BaseScope mCurrentScope;
    private ClassSymbol mCurrentClassSymbol;
    private MethodSymbol mCurrentMethodSymbol;
    private LinkedList<String> mSortedListOfClasses;
    private HashMap<String, EntityLayout> mObjectLayouts;
    private HashMap<String, EntityLayout> mClassLayouts;
    private HashMap<String, EntityLayout> mMethodLayouts;
    private EntityLayout mObjectLayout;
    private EntityLayout mClassLayout;
    private EntityLayout mVirtualTable;

    public NewLayoutsVisitor(BaseScope baseScope, LinkedList<String> sortedListOfClasses) {
        mCurrentScope = baseScope;
        mSortedListOfClasses = sortedListOfClasses;
        mObjectLayouts = new HashMap<>();
        mClassLayouts = new HashMap<>();
        mMethodLayouts = new HashMap<>();
    }

    public HashMap<String, EntityLayout> getObjectLayouts() {
        return mObjectLayouts;
    }

    public HashMap<String, EntityLayout> getClassLayouts() {
        return mClassLayouts;
    }

    public HashMap<String, EntityLayout> getVirtualTables() {
        return mMethodLayouts;
    }

    @Override
    public void visit(Add expression) {
    }

    @Override
    public void visit(And expression) {
    }

    @Override
    public void visit(ArrayAccess expression) {
    }

    @Override
    public void visit(Assignment expression) {
    }

    @Override
    public void visit(AssignmentStatement statement) {
    }

    @Override
    public void visit(Block statement) {
        mCurrentScope = new LocalScope(mCurrentScope, mCurrentScope.getScopeName());

        statement.getStatements().visit(this);

        mCurrentScope = mCurrentScope.getEnclosingScope();
    }

    @Override
    public void visit(BoolLiteral expression) {
    }

    @Override
    public void visit(Break statement) {
    }

    @Override
    public void visit(Continue statement) {
    }

    @Override
    public void visit(Call expression) {
    }

    @Override
    public void visit(ExternalFunctionDeclaration statement) {
    }

    @Override
    public void visit(ExternalCall expression) {
    }

    @Override
    public void visit(CharLiteral expression) {
    }

    @Override
    public void visit(ClassDefinition statement) {
        mCurrentClassSymbol = (ClassSymbol) statement.getSymbol();

        String className = mCurrentClassSymbol.getIdentifier().getName();
        String baseClassName = (mCurrentClassSymbol.getBaseClassIdentifier() == null ? null :  mCurrentClassSymbol.getBaseClassIdentifier().getName());

        mObjectLayout = mObjectLayouts.get(className);

        if (mObjectLayout == null) {
            mObjectLayout = new EntityLayout(mObjectLayouts.get(baseClassName));
            mObjectLayouts.put(className, mObjectLayout);
        }

        mClassLayout = mClassLayouts.get(className);

        if (mClassLayout == null) {
            mClassLayout = new EntityLayout(mClassLayouts.get(baseClassName));
            mClassLayouts.put(className, mClassLayout);
        }

        mVirtualTable = mMethodLayouts.get(className);

        if (mVirtualTable == null) {
            mVirtualTable = new EntityLayout(mMethodLayouts.get(baseClassName));
            mMethodLayouts.put(className, mVirtualTable);
        }

        mCurrentScope = mCurrentClassSymbol;

        statement.getFieldDefinitions().visit(this);
        statement.getConstructorDefinitions().visit(this);
        statement.getMethodDefinitions().visit(this);

        mCurrentScope = mCurrentClassSymbol.getScope();

        mCurrentClassSymbol = null;
    }

    @Override
    public void visit(ConstructorDefinition statement) {
        mCurrentMethodSymbol = (MethodSymbol) statement.getSymbol();

        mCurrentScope = mCurrentMethodSymbol;

        statement.getBody().visit(this);

        mCurrentMethodSymbol = null;

        mCurrentScope = mCurrentScope.getEnclosingScope();
    }

    @Override
    public void visit(Div expression) {
    }

    @Override
    public void visit(DoWhile statement) {
        statement.getStatement().visit(this);
    }

    @Override
    public void visit(Equal expression) {
    }

    @Override
    public void visit(FieldAccess expression) {
    }

    @Override
    public void visit(FieldDefinition statement) {
        if (statement.isStatic()) {
            mClassLayout.add(((FieldSymbol) statement.getSymbol()).getFieldId());
        } else {
            mObjectLayout.add(((FieldSymbol) statement.getSymbol()).getFieldId());
        }
    }

    @Override
    public void visit(GreaterEqualThan expression) {
    }

    @Override
    public void visit(GreaterThan expression) {
    }

    @Override
    public void visit(Identifier expression) {
    }

    @Override
    public void visit(If statement) {
        statement.getThenStatement().visit(this);
        if (statement.getElseStatement() != null) {
            statement.getElseStatement().visit(this);
        }
    }

    @Override
    public void visit(IntLiteral expression) {
    }

    @Override
    public void visit(LessEqualThan expression) {
    }

    @Override
    public void visit(LessThan expression) {
    }

    @Override
    public void visit(MethodDefinition statement) {
        mCurrentMethodSymbol = (MethodSymbol) statement.getSymbol();

        mCurrentScope = mCurrentMethodSymbol;

        // The current method must have access modifier `public` or `protected` and must be non-static. In this case this method can be added to a virtual table.
        if ((!mCurrentMethodSymbol.isPrivate()) && (!mCurrentMethodSymbol.isStatic())) {
            boolean isMethodOverridden = false;

            BaseScope scope = mCurrentClassSymbol.getEnclosingScope();

            Symbol symbol;

            // Scan all classes (scopes) in hierarchy.
            while (scope != null) {
                symbol = scope.getSymbolInCurrentScope(statement.getIdentifier().getName());

                if (symbol instanceof MethodSymbol) {
                    // Try to find a method in a base class. The method in the base class must be `public` or `protected`, has the same numbers of formal arguments, has the same name and has the same return type.
                    while (symbol != null) {
                        if ((((MethodSymbol) symbol).getAccessModifier() == mCurrentMethodSymbol.getAccessModifier()) &&
                                (((MethodSymbol) symbol).isStatic() == mCurrentMethodSymbol.isStatic()) &&
                                (((MethodType) mCurrentMethodSymbol.getType()).isEqualFormalArguments((MethodType) symbol.getType())) &&
                                (((MethodType) mCurrentMethodSymbol.getType()).getReturnType().isEqual(((MethodType) symbol.getType()).getReturnType()))) {
                            // Get a virtual table of a base class.
                            EntityLayout baseVirtualTable = mMethodLayouts.get(((ClassSymbol) scope).getIdentifier().getName());
                            // Use position of the method in the virtual table of base class for the current method.
                            mVirtualTable.add(mCurrentMethodSymbol.getMethodId(), baseVirtualTable.get(((MethodSymbol) symbol).getMethodId()));
                            isMethodOverridden = true;
                            break;
                        }

                        symbol = symbol.getNextSymbol();
                    }
                }

                scope = scope.getEnclosingScope();
            }

            // If a method is not found in a base class then add the current method at the end of list.
            if (!isMethodOverridden) {
                mVirtualTable.add(mCurrentMethodSymbol.getMethodId());
            }
        }

        statement.getBody().visit(this);

        mCurrentMethodSymbol = null;

        mCurrentScope = mCurrentScope.getEnclosingScope();
    }

    @Override
    public void visit(Mod expression) {
    }

    @Override
    public void visit(NewArray expression) {
    }

    @Override
    public void visit(NewObject expression) {
    }

    @Override
    public void visit(Nil expression) {
    }

    @Override
    public void visit(Not expression) {
    }

    @Override
    public void visit(NotEqual expression) {
    }

    @Override
    public void visit(Or expression) {
    }

    @Override
    public void visit(Program statement) {
        HashMap<String, ClassDefinition> allClasses = new HashMap<>();

        Iterator<Statement> listOfClassesIterator = statement.getStatementsList().iterator();

        while (listOfClassesIterator.hasNext()) {
            ClassDefinition classDefinition = (ClassDefinition) listOfClassesIterator.next();
            allClasses.put(classDefinition.getClassIdentifier().getName(), classDefinition);
        }

        Iterator<String> sortedListOfClassesIterator = mSortedListOfClasses.iterator();

        while (sortedListOfClassesIterator.hasNext()) {
            allClasses.get(sortedListOfClassesIterator.next()).visit(this);
        }

        System.out.println();
    }

    @Override
    public void visit(Return statement) {
    }

    @Override
    public void visit(Statements statement) {
        Iterator<Statement> iterator = statement.getStatementsList().iterator();

        while (iterator.hasNext()) {
            iterator.next().visit(this);
        }
    }

    @Override
    public void visit(StringLiteral expression) {
    }

    @Override
    public void visit(Subtract expression) {
    }

    @Override
    public void visit(Super expression) {
    }

    @Override
    public void visit(TernaryOperator expression) {
    }

    @Override
    public void visit(This expression) {
    }

    @Override
    public void visit(Times expression) {
    }

    @Override
    public void visit(UnaryMinus expression) {
    }

    @Override
    public void visit(VariableDefinition statement) {
    }

    @Override
    public void visit(While statement) {
        statement.getStatement().visit(this);
    }

}
