package bee.lang.assembly;

import bee.lang.Constants;

import java.util.HashMap;
import java.util.LinkedList;

public class MipsCodeCreator extends CodeCreator {

    private LinkedList<String> mListOfStrings;
    private LinkedList<String> mListOfVtables;
    private LinkedList<String> mListOfClassDescriptors;
    private LinkedList<String> mListOfProcedures;
    private StringBuilder mRuntimeCode;
    private String mEntryPoint;
    private LinkedList<String> mListOfMethodsInitStaticFields;

    public MipsCodeCreator() {
        mListOfStrings = new LinkedList<>();
        mListOfVtables = new LinkedList<>();
        mListOfClassDescriptors = new LinkedList<>();
        mListOfProcedures = new LinkedList<>();
        mRuntimeCode = new StringBuilder();
        mListOfMethodsInitStaticFields = new LinkedList<>();
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

            mListOfVtables.add(String.format(Constants.VTABLE, className) + ": .word " + sb.toString());
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

            mListOfClassDescriptors.add(String.format(Constants.CLASS_DESCRIPTION, className) + ": .word " + String.format(Constants.VTABLE, className) + " " + sb.toString());
        }
    }

    @Override
    public void addEntryPoint(String entryPoint) {
        mEntryPoint = entryPoint;
    }

    @Override
    public void addProceduresForInitStaticFields(LinkedList<String> listOfProcedures) {
        mListOfMethodsInitStaticFields.addAll(listOfProcedures);
    }

    @Override
    public String create() {
        generateRuntimeLibsCode();

        StringBuilder sb = new StringBuilder();

        sb.append(".data\n");

        sb.append("\n");

        for (String str : mListOfStrings) {
            sb.append(str).append("\n");
        }

        sb.append("\n");

        for (String vtable : mListOfVtables) {
            sb.append(vtable).append("\n");
        }

        sb.append("\n");

        for (String classDescriptor : mListOfClassDescriptors) {
            sb.append(classDescriptor).append("\n");
        }

        sb.append("\n");
        sb.append(".globl main\n");
        sb.append("\n");
        sb.append(".text\n");
        sb.append("\n");

        sb.append("main:\n");

        for (String procedureName : mListOfMethodsInitStaticFields) {
            sb.append("jal ").append(procedureName).append("\n");
        }

        sb.append("jal ").append(mEntryPoint).append("\n");

        sb.append("b _end_program_\n");

        sb.append("\n");

        for (String procedure : mListOfProcedures) {
            sb.append(procedure).append("\n");
        }

        sb.append("\n");

        sb.append(mRuntimeCode.toString()).append("\n");

        sb.append("_end_program_:\n");

        return sb.toString();
    }

    private void generateRuntimeLibsCode() {
        mListOfStrings.add("error_array_out_of_bounds: .asciiz \"Index of array is out of bounds\"");

        mRuntimeCode.append(Constants.FUNCTION_PRINT_ERROR);
        mRuntimeCode.append(":\n");
        mRuntimeCode.append("\tli $s0, 0\n");
        mRuntimeCode.append("\tbne $a0, $s0, _print_error_lbl_false\n");
        mRuntimeCode.append("\tli $v0, 4\n");
        mRuntimeCode.append("\tla $a0, error_array_out_of_bounds\n");
        mRuntimeCode.append("\tsyscall\n");
        mRuntimeCode.append("\t_print_error_lbl_false:\n");
        mRuntimeCode.append("\tb _end_program_\n");

        mRuntimeCode.append("\n");

        mRuntimeCode.append(Constants.FUNCTION_ALLOC_INIT_RAW_MEMORY);
        mRuntimeCode.append(":\n");
        mRuntimeCode.append("\tli $v0, 9\n");
        mRuntimeCode.append("\tsyscall\n");
        mRuntimeCode.append("\tjr $ra\n");

        mRuntimeCode.append("\n");

        mRuntimeCode.append(Constants.FUNCTION_CONVERT_STRING_TO_ARRAY);
        mRuntimeCode.append(":\n");
        mRuntimeCode.append("\t_convert_string_to_array_loop:\n");
        mRuntimeCode.append("\t\tbeqz $a2, _convert_string_to_array_end\n");
        mRuntimeCode.append("\t\tlb $s0, 0($a1)\n");
        mRuntimeCode.append("\t\tsw $s0, 0($a0)\n");
        mRuntimeCode.append("\t\taddi $a1, $a1, 1\n");
        mRuntimeCode.append("\t\taddi $a0, $a0, 4\n");
        mRuntimeCode.append("\t\taddi $a2, $a2, -1\n");
        mRuntimeCode.append("\t\tb _convert_string_to_array_loop\n");
        mRuntimeCode.append("\t_convert_string_to_array_end:\n");
        mRuntimeCode.append("\tjr $ra\n");

        mRuntimeCode.append("\n");

        mRuntimeCode.append(Constants.FUNCTION_PRINT_INTEGER);
        mRuntimeCode.append(":\n");
        mRuntimeCode.append("\tli $v0, 1\n");
        mRuntimeCode.append("\tsyscall\n");
        mRuntimeCode.append("\tjr $ra\n");

        mRuntimeCode.append("\n");

        mRuntimeCode.append(Constants.FUNCTION_PRINT_CHAR);
        mRuntimeCode.append(":\n");
        mRuntimeCode.append("\tli $v0, 11\n");
        mRuntimeCode.append("\tsyscall\n");
        mRuntimeCode.append("\tjr $ra\n");

        mRuntimeCode.append("\n");

        mRuntimeCode.append(Constants.FUNCTION_READ_INTEGER);
        mRuntimeCode.append(":\n");
        mRuntimeCode.append("\tli $v0, 5\n");
        mRuntimeCode.append("\tsyscall\n");
        mRuntimeCode.append("\tjr $ra\n");

        mRuntimeCode.append("\n");

        mRuntimeCode.append(Constants.FUNCTION_READ_CHAR);
        mRuntimeCode.append(":\n");
        mRuntimeCode.append("\tli $v0, 12\n");
        mRuntimeCode.append("\tsyscall\n");
        mRuntimeCode.append("\tjr $ra\n");

        mRuntimeCode.append("\n");

        mRuntimeCode.append(Constants.FUNCTION_STR_LEN);
        mRuntimeCode.append(":\n");
        mRuntimeCode.append(Constants.FUNCTION_ARRAY_LEN);
        mRuntimeCode.append(":\n");
        mRuntimeCode.append("\tlw $v0, 0($a0)\n");
        mRuntimeCode.append("\tjr $ra\n");
    }
    
}
