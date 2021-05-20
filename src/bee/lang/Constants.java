package bee.lang;

public class Constants {

    // Function to print an error message.
    public static final String FUNCTION_PRINT_ERROR = "_print_error";
    // Arguments for the function '_print_error'.
    // If the first argument equals 0 this means an index is out of size of an array.

    // Function to allocate and initialize block of raw memory.
    public static final String FUNCTION_ALLOC_INIT_RAW_MEMORY = "_alloc_init_block";

    // Template of function name to initialize all non-static fields for an object.
    public static final String FUNCTION_INIT_FIELDS = "_%s_init_fields";

    // Template of function name to initialize static fields.
    public static final String FUNCTION_INIT_STATIC_FIELDS = "_%s_init_static_fields";

    // Function to allocate and initialize block of memory with chars.
    public static final String FUNCTION_CONVERT_STRING_TO_ARRAY = "_convert_string_to_array";

    // Function to print integer.
    public static final String FUNCTION_PRINT_INTEGER = "print_int";

    // Function to print char.
    public static final String FUNCTION_PRINT_CHAR = "print_char";

    // Function to read integer.
    public static final String FUNCTION_READ_INTEGER = "read_int";

    // Function to read char.
    public static final String FUNCTION_READ_CHAR = "read_char";

    public static final String CLASS_DESCRIPTION = "_%s_class_description_";

    public static final String VTABLE = "%s_vtable";

    private Constants() {
    }

}
