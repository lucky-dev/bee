package bee.lang;

import bee.lang.assembly.AsmInstruction;
import bee.lang.assembly.CodeCreator;
import bee.lang.assembly.MipsCodeCreator;
import bee.lang.ast.Program;
import bee.lang.ir.tree.IRStatement;
import bee.lang.lexer.Lexer;
import bee.lang.parser.Parser;
import bee.lang.semanalysis.*;
import bee.lang.symtable.BaseScope;
import bee.lang.translate.*;
import bee.lang.translate.frame.Frame;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
        // Lexical analysis
        Lexer lexer = new Lexer();
        // Parsing
        Parser parser = new Parser(lexer);
        // Build AST
        Program program = parser.parse("class Main { static var z : int = 1; constructor() : super() { } static main() { Main.z = 5; var a : A = new B(); var x : int = a.f(); } } class A { constructor() : super() { } f() : int { return 1; } } class B : A { constructor() : super() { } f1() : int { return 2; } }");
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
        NewIRTreeVisitor newIRTreeVisitor = new NewIRTreeVisitor(newLayoutsVisitor.getObjectLayouts(), newLayoutsVisitor.getClassLayouts(), newLayoutsVisitor.getVirtualTables());
        newIRTreeVisitor.visit(program);

        TransformIRTree transformIRTree = new TransformIRTree();

        CodeCreator codeCreator = new MipsCodeCreator();
        codeCreator.addVtables(codeCreator.generateVtables(newLayoutsVisitor.getVirtualTables()));
        codeCreator.addClassDescriptors(codeCreator.getCountOfStaticFields(newLayoutsVisitor.getClassLayouts()));

        for (Fragment fragment : newIRTreeVisitor.getFragments()) {
            if (fragment instanceof StringFragment) {
                StringFragment stringFragment = (StringFragment) fragment;
                codeCreator.addString(stringFragment.getLabel(), stringFragment.getStr());
            }
        }

        for (Fragment fragment : newIRTreeVisitor.getFragments()) {
            if (fragment instanceof ProcedureFragment) {
                ProcedureFragment procedureFragment = (ProcedureFragment) fragment;
                IRStatement procedureBody = procedureFragment.getBody();
                // Translation to IR
                IRStatement canonicalTrees = transformIRTree.transformStatement(procedureBody);
                LinkedList<IRStatement> linearizedTree = transformIRTree.linearizeTree(canonicalTrees);
                // Create basic blocks and trace
                Frame frame = procedureFragment.getFrame();
                ControlFlowAnalyzing controlFlowAnalyzing = new ControlFlowAnalyzing(frame.getProcedureName());
                LinkedList<IRStatement> tracedTrees = controlFlowAnalyzing.trace(linearizedTree);
                LinkedList<AsmInstruction> asmInstructions = new LinkedList<>();
                for (IRStatement statement : tracedTrees) {
                    // Instruction selection
                    asmInstructions.addAll(frame.codegen(statement));
                }

                asmInstructions = frame.procEntryExit2(asmInstructions);

                // Liveness analysis and register allocation
                RegAlloc regAlloc = new RegAlloc(frame, asmInstructions);

                frame.procEntryExit3(asmInstructions);

                StringBuilder procedure = new StringBuilder();
                for (AsmInstruction asmInstruction : asmInstructions) {
                    String instruction = asmInstruction.format(regAlloc);
                    if (!instruction.isEmpty()) {
                        procedure.append(instruction);
                        procedure.append("\n");
                    }
                }

                codeCreator.addProcedure(procedure.toString());
            }
        }

        System.out.println(codeCreator.create());
    }

}
