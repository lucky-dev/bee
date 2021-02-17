package bee.lang.ast;

import java.util.LinkedList;

public class ArgumentsList {

    private LinkedList<Expression> mExpressionList;

    public ArgumentsList() {
        mExpressionList = new LinkedList<>();
    }

    public void addExpression(Expression expression) {
        mExpressionList.add(expression);
    }

    public LinkedList<Expression> getExpressionList() {
        return mExpressionList;
    }

}
