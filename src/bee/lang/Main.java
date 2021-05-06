package bee.lang;

import bee.lang.assembly.AsmInstruction;
import bee.lang.ast.Program;
import bee.lang.ir.tree.IRStatement;
import bee.lang.lexer.Lexer;
import bee.lang.parser.Parser;
import bee.lang.semanalysis.*;
import bee.lang.symtable.BaseScope;
import bee.lang.translate.*;
import bee.lang.translate.frame.MipsFrame;

import java.util.Iterator;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
        // Lexical analysis
        Lexer lexer = new Lexer();
        // Parsing
        Parser parser = new Parser(lexer);
        // Build AST
        Program program = parser.parse("");
        // Semantic analysis
        NewSymbolTableVisitor symbolTableVisitor = new NewSymbolTableVisitor();
        symbolTableVisitor.visit(program);
        BaseScope scope = symbolTableVisitor.getCurrentScope();
        ValidatingMethodsVisitor validatingMethodsVisitor = new ValidatingMethodsVisitor(scope);
        validatingMethodsVisitor.visit(program);
        TypeCheckingVisitor typeCheckingVisitor = new TypeCheckingVisitor(scope);
        typeCheckingVisitor.visit(program);
        ValidatingLoopsVisitor validatingLoopsVisitor = new ValidatingLoopsVisitor();
        validatingLoopsVisitor.visit(program);
        NewLayoutsVisitor newLayoutsVisitor = new NewLayoutsVisitor(scope, symbolTableVisitor.getSortedListOfClasses());
        newLayoutsVisitor.visit(program);
        // Translation to IR
        NewIRTreeVisitor newIRTreeVisitor = new NewIRTreeVisitor(new MipsFrame(), newLayoutsVisitor.getObjectLayout(), newLayoutsVisitor.getClassLayout(), newLayoutsVisitor.getVirtualTable());
        newIRTreeVisitor.visit(program);

        TransformIRTree transformIRTree = new TransformIRTree();

        Iterator<Fragment> fragmentIterator = newIRTreeVisitor.getFragment().iterator();
        while (fragmentIterator.hasNext()) {
            Fragment fragment = fragmentIterator.next();

//            if (fragment instanceof DataFragment) {
//                System.out.println("=== START DATA ===");
//                System.out.println(fragment);
//                System.out.println("=== END DATA ===");
//            }

            if (fragment instanceof ProcedureFragment) {
                System.out.println("=== START PROCEDURE ===");
                ProcedureFragment procedureFragment = (ProcedureFragment) fragment;
                IRStatement procedureBody = procedureFragment.getBody();
                // Translation to IR
                IRStatement canonicalTrees = transformIRTree.transformStatement(procedureBody);
                LinkedList<IRStatement> linearizedTree = transformIRTree.linearizeTree(canonicalTrees);
                // Create basic blocks and trace
                ControlFlowAnalyzing controlFlowAnalyzing = new ControlFlowAnalyzing(procedureFragment.getFrame().getProcedureName());
                LinkedList<IRStatement> tracedTrees = controlFlowAnalyzing.trace(linearizedTree);
                LinkedList<AsmInstruction> asmInstructions = new LinkedList<>();
                for (IRStatement statement : tracedTrees) {
                    // Instruction selection
                    asmInstructions.addAll(procedureFragment.getFrame().codegen(statement));
                }
                asmInstructions = procedureFragment.getFrame().procEntryExit2(asmInstructions);

                // Liveness analysis and register allocation
                RegAlloc regAlloc = new RegAlloc(procedureFragment.getFrame(), asmInstructions);

                for (AsmInstruction asmInstruction : asmInstructions) {
                    System.out.println(asmInstruction.format(regAlloc));
                }

                System.out.println("=== END PROCEDURE ===");
            }
        }
    }

}
