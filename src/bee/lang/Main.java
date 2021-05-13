package bee.lang;

import bee.lang.assembly.AsmInstruction;
import bee.lang.assembly.CodeCreator;
import bee.lang.assembly.MipsCodeCreator;
import bee.lang.ast.Program;
import bee.lang.exceptions.*;
import bee.lang.ir.tree.IRStatement;
import bee.lang.lexer.Lexer;
import bee.lang.parser.Parser;
import bee.lang.semanalysis.*;
import bee.lang.symtable.BaseScope;
import bee.lang.translate.*;
import bee.lang.translate.frame.Frame;
import bee.lang.translate.frame.MipsFrame;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class Main {

    // Example to run compiler: <a jar file of compiler> -s <path to a source file or a directory of source files with the extension .bee> -d <path to an output file>
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Need a source file or directory with source files and a destination file.");
            return;
        }

        if (args.length < 4) {
            System.out.println("Not enough arguments.");
            return;
        }

        LinkedList<String> pathOfSrcFiles = new LinkedList<>();

        String mDestinationFile = "";

        if (args.length == 4) {
            for (int i = 0; i < args.length; i++) {
                if ((args[i].equals("-s")) && (i + 1 < args.length)) {
                    File inputFile = new File(args[i + 1]);

                    File[] files = inputFile.isFile() ? new File[] { inputFile } : inputFile.listFiles();

                    if (files == null) {
                        files = new File[0];
                    }

                    for (File file : files) {
                        if (file.getName().endsWith(".bee")) {
                            pathOfSrcFiles.add(file.getAbsolutePath());
                        }
                    }
                }

                if ((args[i].equals("-d")) && (i + 1 < args.length)) {
                    mDestinationFile = args[i + 1];
                }
            }
        }

        try {
            // Lexical analysis
            Lexer lexer = new Lexer();
            // Parsing
            Parser parser = new Parser(lexer);
            // Build AST
            Program program = parser.parse(pathOfSrcFiles);
            // Semantic analysis
            NewSymbolTableVisitor symbolTableVisitor = new NewSymbolTableVisitor();
            symbolTableVisitor.createSymbolTable(program);
            BaseScope scope = symbolTableVisitor.getCurrentScope();
            ValidatingMethodsVisitor validatingMethodsVisitor = new ValidatingMethodsVisitor(scope);
            validatingMethodsVisitor.validateMethods(program);
            TypeCheckingVisitor typeCheckingVisitor = new TypeCheckingVisitor(scope);
            typeCheckingVisitor.check(program);
            ValidatingLoopsVisitor validatingLoopsVisitor = new ValidatingLoopsVisitor();
            validatingLoopsVisitor.validateLoops(program);
            NewLayoutsVisitor newLayoutsVisitor = new NewLayoutsVisitor(scope, symbolTableVisitor.getSortedListOfClasses());
            newLayoutsVisitor.visit(program);
            // Translation to IR
            NewIRTreeVisitor newIRTreeVisitor = new NewIRTreeVisitor(new MipsFrame(), newLayoutsVisitor.getObjectLayouts(), newLayoutsVisitor.getClassLayouts(), newLayoutsVisitor.getVirtualTables());
            newIRTreeVisitor.visit(program);

            TransformIRTree transformIRTree = new TransformIRTree();

            CodeCreator codeCreator = new MipsCodeCreator();
            codeCreator.addVtables(codeCreator.generateVtables(newLayoutsVisitor.getVirtualTables()));
            codeCreator.addClassDescriptors(codeCreator.getCountOfStaticFields(newLayoutsVisitor.getClassLayouts()));
            codeCreator.addEntryPoint(validatingMethodsVisitor.getEntryPointMethod().getMethodId());
            codeCreator.addProceduresForInitStaticFields(newIRTreeVisitor.getListOfMethodsInitStaticFields());

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
                            procedure.append(instruction).append("\n");
                        }
                    }

                    codeCreator.addProcedure(procedure.toString());
                }
            }

            String code = codeCreator.create();

            FileWriter fileWriter = new FileWriter(mDestinationFile);
            fileWriter.write(code);
            fileWriter.close();
        } catch (BaseParserException |
                SymbolTableException |
                ValidatingMethodsException |
                TypeCheckingException |
                ValidatingLoopsException |
                SelectColorException |
                CodegenException e) {
            System.out.println(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
