package bee.lang.exceptions;

import bee.lang.ir.Temp;

public class SelectColorException extends Exception {

    public SelectColorException(Temp temp) {
        super("Can not select color for temporary variable " + temp.toString());
    }

}
