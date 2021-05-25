package bee.lang.translate;

import bee.lang.ir.Temp;
import bee.lang.ir.tree.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

// This class is used for transforming IR-tree to canonical IR-tree. Canonical tree has two properties:
// 1. No SEQ and ESEQ nodes.
// 2. The node CALL is inside EXP(CALL(f, args)) or MOVE(TEMP(t), CALL(f, args)).
// Canonical trees are very convenient. These trees can be used for further processing (e.g. creating linearized code).
public class TransformIRTree {

    public IRStatement transformStatement(IRStatement statement) {
        if (statement instanceof SEQ) {
            SEQ seq = (SEQ) statement;
            IRStatement irLeftStatement = transformStatement(seq.getLeftStatement());
            IRStatement irRightStatement = transformStatement(seq.getRightStatement());

            if ((!isNop(irLeftStatement)) && (!isNop(irRightStatement))) {
                return new SEQ(irLeftStatement, irRightStatement);
            } else if (!isNop(irLeftStatement)) {
                return irLeftStatement;
            } else {
                return irRightStatement;
            }
        }

        if (statement instanceof MOVE) {
            MOVE move = (MOVE) statement;

            if (move.getDst() instanceof ESEQ) {
                ESEQ eseqDst = (ESEQ) move.getDst();
                return transformStatement(new SEQ(eseqDst.getStatement(), new MOVE(eseqDst.getExpression(), move.getSrc())));
            }
        }

        SELPair selPair;
        IRStatement irStatement;
        if ((statement instanceof MOVE) && (((MOVE) statement).getDst() instanceof TEMP) && (((MOVE) statement).getSrc() instanceof CALL)) {
            IRExpression call = ((MOVE) statement).getSrc();
            selPair = reorder(call.kids());
            irStatement = new MOVE(((MOVE) statement).getDst(), call.build(selPair.getListExpressions()));
        } else if ((statement instanceof EXP) && (((EXP) statement).getExpression() instanceof CALL)) {
            IRExpression call = ((EXP) statement).getExpression();
            selPair = reorder(call.kids());
            irStatement = new EXP(call.build(selPair.getListExpressions()));
        } else {
            selPair = reorder(statement.kids());
            irStatement = statement.build(selPair.getListExpressions());
        }

        return isNop(selPair.getStatement()) ? irStatement : new SEQ(selPair.getStatement(), irStatement);
    }

    public IRExpression transformExpression(IRExpression expression) {
        if (expression instanceof ESEQ) {
            ESEQ eseq = (ESEQ) expression;
            IRStatement transformedIRStatement = transformStatement(eseq.getStatement());
            IRExpression transformedIRExpression = transformExpression(eseq.getExpression());

            if (transformedIRExpression instanceof ESEQ) {
                ESEQ eseqTransformed = (ESEQ) transformedIRExpression;
                if ((!isNop(transformedIRStatement)) && (!isNop(eseqTransformed.getStatement()))) {
                    return new ESEQ(new SEQ(transformedIRStatement, eseqTransformed.getStatement()), eseqTransformed.getExpression());
                } else if (!isNop(transformedIRStatement)) {
                    return new ESEQ(transformedIRStatement, eseqTransformed.getExpression());
                } else {
                    return new ESEQ(eseqTransformed.getStatement(), eseqTransformed.getExpression());
                }
            } else {
                if (isNop(transformedIRStatement)) {
                    return transformedIRExpression;
                } else {
                    return new ESEQ(transformedIRStatement, transformedIRExpression);
                }
            }
        }

        if (expression instanceof CALL) {
            CALL call = (CALL) expression;
            TEMP temp = new TEMP(new Temp());
            return transformExpression(new ESEQ(new MOVE(temp, call), temp));
        }

        SELPair selPair = reorder(expression.kids());

        return isNop(selPair.getStatement()) ? expression.build(selPair.getListExpressions()) : new ESEQ(selPair.getStatement(), expression.build(selPair.getListExpressions()));
    }

    public LinkedList<IRStatement> linearizeTree(IRStatement irStatement) {
        LinkedList<IRStatement> listStatements = new LinkedList<>();
        extractSeq(irStatement, listStatements);
        return listStatements;
    }

    private SELPair reorder(LinkedList<IRExpression> kids) {
        if (kids == null) {
            return new SELPair();
        }

        ArrayList<IRExpression> listTransformedExpressions = new ArrayList<>();

        for (IRExpression expression : kids) {
            listTransformedExpressions.add(transformExpression(expression));
        }

        LinkedList<IRExpression> listExpressions = new LinkedList<>();
        LinkedList<IRStatement> listStatements = new LinkedList<>();

        int i;
        for (i = listTransformedExpressions.size() - 1; i > -1; i--) {
            IRExpression irExpression = listTransformedExpressions.get(i);

            if (irExpression instanceof ESEQ) {
                ESEQ eseq = (ESEQ) irExpression;

                if (i - 1 > -1) {
                    IRExpression nextIRExpression = listTransformedExpressions.get(i - 1);

                    listStatements.addFirst(eseq.getStatement());
                    listExpressions.addFirst(eseq.getExpression());

                    if (nextIRExpression instanceof ESEQ) {
                        nextIRExpression = ((ESEQ) nextIRExpression).getExpression();
                    }

                    if (!isCommute(eseq.getStatement(), nextIRExpression)) {
                        break;
                    }
                } else {
                    listStatements.addFirst(eseq.getStatement());
                    listExpressions.addFirst(eseq.getExpression());
                }
            } else {
                listExpressions.addFirst(irExpression);
            }
        }

        for (int j = i - 1; j > -1; j--) {
            IRExpression irExpression = listTransformedExpressions.get(j);

            if (irExpression instanceof ESEQ) {
                ESEQ seq = (ESEQ) irExpression;

                TEMP newTemp = new TEMP(new Temp());
                listStatements.addFirst(new SEQ(seq.getStatement(), new MOVE(newTemp, seq.getExpression())));
                listExpressions.addFirst(newTemp);
            } else {
                listExpressions.addFirst(irExpression);
            }
        }

        IRStatement statement;
        Iterator<IRStatement> iterator = listStatements.descendingIterator();
        if (iterator.hasNext()) {
            // Create single statement SEQ.
            statement = iterator.next();
            while (iterator.hasNext()) {
                statement = new SEQ(iterator.next(), statement);
            }
        } else {
            statement = new EXP(new CONST(0));
        }

        return new SELPair(statement, listExpressions);
    }

    // This function checks statement and expression and decides it is possible or not possible to move statement before expression.
    private boolean isCommute(IRStatement statement, IRExpression expression) {
        return ((isNop(statement)) ||
                (expression instanceof NAME) ||
                (expression instanceof CONST));
    }

    // NOP means 'no-operation'. E.g. EXP(CONST(0)) does nothing.
    private boolean isNop(IRStatement statement) {
        return ((statement instanceof EXP) && (((EXP) statement).getExpression() instanceof CONST));
    }

    private static class SELPair {

        // 'mStatement' contains all statements which must be executed before expressions in the list 'mListExpressions'.
        // 'mListExpressions' contains expressions without ESEQ nodes.
        private IRStatement mStatement;
        private LinkedList<IRExpression> mListExpressions;

        public SELPair(IRStatement statement, LinkedList<IRExpression> listExpressions) {
            mStatement = statement;
            mListExpressions = listExpressions;
        }

        public SELPair() {
            this(new EXP(new CONST(0)), new LinkedList<>());
        }

        public IRStatement getStatement() {
            return mStatement;
        }

        public LinkedList<IRExpression> getListExpressions() {
            return mListExpressions;
        }

    }

    private void extractSeq(IRStatement irStatement, LinkedList<IRStatement> list) {
        if (irStatement instanceof SEQ) {
            SEQ seq = (SEQ) irStatement;
            extractSeq(seq.getLeftStatement(), list);
            extractSeq(seq.getRightStatement(), list);
        } else {
            list.add(irStatement);
        }
    }

}
