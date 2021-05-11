package bee.lang.assembly;

import bee.lang.translate.EntityLayout;

import java.util.HashMap;
import java.util.LinkedList;

public abstract class CodeCreator {

    public abstract void addString(String label, String str);
    public abstract void addVtables(HashMap<String, LinkedList<String>> vtables);
    public abstract void addProcedure(String procedure);
    public abstract void addClassDescriptors(HashMap<String, Integer> staticFields);
    public abstract String create();

    public HashMap<String, LinkedList<String>> generateVtables(HashMap<String, EntityLayout> methodLayouts) {
        HashMap<String, LinkedList<String>> vtables = new HashMap<>();

        for (String className : methodLayouts.keySet()) {
            EntityLayout entityLayout = methodLayouts.get(className);
            String[] virtualMethods = new String[entityLayout.getCountItems()];

            while (entityLayout != null) {
                for (String methodName : entityLayout.getKeys()) {
                    if (virtualMethods[entityLayout.get(methodName)] == null) {
                        virtualMethods[entityLayout.get(methodName)] = methodName;
                    }
                }

                entityLayout = entityLayout.getPrevious();
            }

            LinkedList<String> methods = new LinkedList<>();
            for (int i = 0; i < virtualMethods.length; i++) {
                methods.add(virtualMethods[i]);
            }

            vtables.put(className, methods);
        }

        return vtables;
    }

    public HashMap<String, Integer> getCountOfStaticFields(HashMap<String, EntityLayout> classLayouts) {
        HashMap<String, Integer> classDescriptors = new HashMap<>();

        for (String className : classLayouts.keySet()) {
            classDescriptors.put(className, classLayouts.get(className).getCountItems());
        }

        return classDescriptors;
    }

}
