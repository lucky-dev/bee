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

import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {
        // Lexical analysis
        Lexer lexer = new Lexer();
        // Parsing
        Parser parser = new Parser(lexer);
//        Program program = parser.parse("class A { var y : A; var z : int[]; var m : int = 2; static var x : int = 1; constructor(var x : int, var y : int, var z : int) : super() { A.x = 1; if (true) { var s : char[] = \"test\"; } } constructor(var x : int, var y : int) : (x, y, 0) { } f(var x : int, var y : int) : int { var z : int; z = 1; x = 2; y = 3; return z; } f1() { } }");
//        Program program = parser.parse("class A { var x : int = 3; static var y : int = 7; constructor() : super() { } f() { } }");
//        Program program = parser.parse("class A { constructor() : super() { } f() { var x : int = 123; var y : int = 7; var z : int; z = x + y; var arr : int[] = new int[1]; arr[0] = 1; x = 2 + f1(5) + 1; if (1 > 2) { } while (2 < 1) { } do { } while(1 < 2); return; } f1(var x : int) : int { return x; } }");
        // Build AST
        Program program = parser.parse("class Main { static var z : int = 3; var x : int = 4; constructor() : super() { } static main() { Main.z = 5; var obj : Main = new Main(); obj.x = 6; } f1() { var i : int = 0; i = f2(1, 2); } f2(var x : int, var y : int) : int { return x + y; } } class B : Main { constructor() : super() { } }");
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
