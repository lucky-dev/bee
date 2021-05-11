package bee.lang.assembly;

import java.util.HashMap;
import java.util.LinkedList;

public class MipsCodeCreator extends CodeCreator {

    private final String ENTRY_POINT = "Main_method_main_0"; //"main";

    private LinkedList<String> mListOfStrings;
    private LinkedList<String> mListOfVtables;
    private LinkedList<String> mListOfClassDescriptors;
    private LinkedList<String> mListOfProcedures;

    public MipsCodeCreator() {
        mListOfStrings = new LinkedList<>();
        mListOfVtables = new LinkedList<>();
        mListOfClassDescriptors = new LinkedList<>();
        mListOfProcedures = new LinkedList<>();
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
        StringBuilder sb = new StringBuilder();

        sb.append(".data");
        sb.append("\n");

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

        sb.append("_end_program_");

        return sb.toString();
    }
    
}
