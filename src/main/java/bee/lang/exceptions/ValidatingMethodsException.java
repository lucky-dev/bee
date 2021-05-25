package bee.lang.exceptions;

public class ValidatingMethodsException extends Exception {

    public ValidatingMethodsException() {
        this("");
    }

    public ValidatingMethodsException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return getMessage().isEmpty() ? "" : super.toString();
    }

}
