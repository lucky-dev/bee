package bee.lang.assembly;

import java.util.HashMap;
import java.util.LinkedList;

public class MipsCodeCreator extends CodeCreator {

    private final String ENTRY_POINT = "main";

    private LinkedList<String> mListOfStrings;
    private LinkedList<String> mListOfVtables;
    private LinkedList<String> mListOfClassDescriptors;
    private LinkedList<String> mListOfProcedures;
    private StringBuilder mRuntimeCode;

    public MipsCodeCreator() {
        mListOfStrings = new LinkedList<>();
        mListOfVtables = new LinkedList<>();
        mListOfClassDescriptors = new LinkedList<>();
        mListOfProcedures = new LinkedList<>();
        mRuntimeCode = new StringBuilder();
    }
    
    @Override
    public void addString(String label, String str) {
        mListOfStrings.add(label + ": .asciiz \"" + str + "\"");
    }

    @Override
    public void addProcedure(String procedure) {
        mListOfProcedures.add(procedure);
    }

    @Override
    public void addVtables(HashMap<String, LinkedList<String>> vtables) {
        for (String className : vtables.keySet()) {
            StringBuilder sb = new StringBuilder();

            LinkedList<String> methods = vtables.get(className);
            for (String method : methods) {
                sb.append(method);

                if (!methods.getLast().equals(method)) {
                    sb.append(", ");
                }
            }

            if (methods.isEmpty()) {
                sb.append("0");
            }

            mListOfVtables.add(className + "_vtable: .word " + sb.toString());
        }
    }

    @Override
    public void addClassDescriptors(HashMap<String, Integer> staticFields) {
        for (String className : staticFields.keySet()) {
            StringBuilder sb = new StringBuilder();

            int countStaticFields = staticFields.get(className);
            for (int i = 0; i < countStaticFields; i++) {
                sb.append(", 0");
            }

            mListOfClassDescriptors.add("_" + className + "_class_description_: .word " + className + "_vtable " + sb.toString());
        }
    }

    @Override
    public String create() {
        generateRuntimeLibsCode();

        StringBuilder sb = new StringBuilder();

        sb.append(".data");
        sb.append("\n\n");

        for (String str : mListOfStrings) {
            sb.append(str);
            sb.append("\n");
        }

        sb.append("\n");

        for (String vtable : mListOfVtables) {
            sb.append(vtable);
            sb.append("\n");
        }

        sb.append("\n");

        for (String classDescriptor : mListOfClassDescriptors) {
            sb.append(classDescriptor);
            sb.append("\n");
        }

        sb.append("\n");
        sb.append(".globl ");
        sb.append(ENTRY_POINT);
        sb.append("\n\n");
        sb.append(".text");
        sb.append("\n\n");

        for (String procedure : mListOfProcedures) {
            sb.append(procedure);
            sb.append("\n");
        }

        sb.append("\n");
        sb.append(mRuntimeCode.toString());
        sb.append("\n\n");
        sb.append("_end_program_:");

        return sb.toString();
    }

    private void generateRuntimeLibsCode() {
        mListOfStrings.add("error_array_out_of_bounds: .asciiz \"Index of array is out of bounds\"");

        mRuntimeCode.append("_print_error:\n");
        mRuntimeCode.append("\tli $t0, 0\n");
        mRuntimeCode.append("\tbne $a0, $t0, _print_error_lbl_false\n");
        mRuntimeCode.append("\tli $v0, 4\n");
        mRuntimeCode.append("\tla $a0, error_array_out_of_bounds\n");
        mRuntimeCode.append("\tsyscall\n");
        mRuntimeCode.append("\t_print_error_lbl_false:\n");
        mRuntimeCode.append("\tb _end_program_\n");

        mRuntimeCode.append("\n");

        mRuntimeCode.append("_alloc_init_block:\n");
        mRuntimeCode.append("\tli $v0, 9\n");
        mRuntimeCode.append("\tsyscall\n");
        mRuntimeCode.append("\tjr $ra\n");

        mRuntimeCode.append("\n");

        mRuntimeCode.append("_convert_string_to_array:\n");
        mRuntimeCode.append("\tli $v0, 9\n");
        mRuntimeCode.append("\tsyscall\n");
        mRuntimeCode.append("\tmove $t0, $v0\n");
        mRuntimeCode.append("\t_convert_string_to_array_loop:\n");
        mRuntimeCode.append("\t\tlb $t1, 0($a1)\n");
        mRuntimeCode.append("\t\tsw $t1, 0($t0)\n");
        mRuntimeCode.append("\t\taddi $a1, $a1, 1\n");
        mRuntimeCode.append("\t\taddi $t0, $t0, 4\n");
        mRuntimeCode.append("\t\tbnez $t1, _convert_string_to_array_loop\n");
        mRuntimeCode.append("\tjr $ra\n");
    }
    
}








