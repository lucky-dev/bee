package bee.lang;

import bee.lang.ast.Program;
import bee.lang.lexer.Lexer;
import bee.lang.parser.Parser;
import bee.lang.semanalysis.TypeCheckingVisitor;
import bee.lang.semanalysis.ValidatingMethodsSymbolTableVisitor;
import bee.lang.symtable.BaseScope;
import bee.lang.semanalysis.NewSymbolTableVisitor;

public class Main {

    public static void main(String[] args) {

        Parser parser = new Parser(new Lexer());
        Program program = parser.parse("");
        NewSymbolTableVisitor symbolTableVisitor = new NewSymbolTableVisitor();
        symbolTableVisitor.visit(program);
        BaseScope scope = symbolTableVisitor.getCurrentScope();
        ValidatingMethodsSymbolTableVisitor validatingMethodsSymbolTableVisitor = new ValidatingMethodsSymbolTableVisitor(scope);
        validatingMethodsSymbolTableVisitor.visit(program);
        TypeCheckingVisitor typeCheckingVisitor = new TypeCheckingVisitor(scope);
        typeCheckingVisitor.visit(program);

        System.out.println();
    }

}
