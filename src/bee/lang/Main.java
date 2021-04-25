package bee.lang;

import bee.lang.assembly.AsmInstruction;
import bee.lang.assembly.TempMap;
import bee.lang.ast.Program;
import bee.lang.ir.Temp;
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
        Parser parser = new Parser(new Lexer());
        Program program = parser.parse("");
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
        NewIRTreeVisitor newIRTreeVisitor = new NewIRTreeVisitor(new MipsFrame(), newLayoutsVisitor.getObjectLayout(), newLayoutsVisitor.getClassLayout(), newLayoutsVisitor.getVirtualTable());
        newIRTreeVisitor.visit(program);

        printAllFragments(newIRTreeVisitor);
    }

    private static void printAllFragments(NewIRTreeVisitor newIRTreeVisitor) {
        TempMap tempMap = new TempMap() {
            @Override
            public String tempMap(Temp temp) {
                return null;
            }
        };

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
                IRStatement canonicalTrees = transformIRTree.transformStatement(procedureBody);
                LinkedList<IRStatement> linearizedTree = transformIRTree.linearizeTree(canonicalTrees);
                ControlFlowAnalyzing controlFlowAnalyzing = new ControlFlowAnalyzing(procedureFragment.getFrame().getProcedureName());
                LinkedList<IRStatement> tracedTrees = controlFlowAnalyzing.trace(linearizedTree);
                LinkedList<AsmInstruction> asmInstructions = new LinkedList<>();
                for (IRStatement statement : tracedTrees) {
                    LinkedList<AsmInstruction> list = procedureFragment.getFrame().codegen(statement);

                    asmInstructions.addAll(list);

                    for (AsmInstruction asmInstruction : list) {
                        System.out.println(asmInstruction.format(tempMap));
                    }
                }

                System.out.println("\nControl-flow graph:");
                AsmFlowGraph asmFlowGraph = new AsmFlowGraph(asmInstructions);
                asmFlowGraph.print(tempMap);
                System.out.println("\nInterference graph:");
                Liveness liveness = new Liveness(asmFlowGraph);
                liveness.print();
                System.out.println("=== END PROCEDURE ===");
            }
        }
    }

}
