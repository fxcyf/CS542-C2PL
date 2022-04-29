package elements;

import java.io.Serializable;

public class Operation implements Serializable {
    private int sid;
    private int tid;
    public static final int readType = 0;
    public static final int writeType = 1;
    public static final int mathType = 2;
    public static final int commitType = 3;
    private int opType;
    private String arg;
    private String operand1;
    private String operand2;
    private char operator;

    private static final long serialVersionUID = 22L;

    public Operation(int sid, int tid, int opType, String arg) {
        this.sid = sid;
        this.tid = tid;
        this.opType = opType;
        this.arg = arg;
    }

    public Operation(int sid, int tid, int opType) {
        this.sid = sid;
        this.tid = tid;
        this.opType = opType;
    }

    public Operation(int sid, int tid, int opType, String arg, String operand1, String operand2, char operator) {
        this.sid = sid;
        this.tid = tid;
        this.opType = opType;
        this.arg = arg;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operator = operator;
    }

    public int getSID() {
        return sid;
    }

    public int getTID() {
        return tid;
    }

    public int getType() {
        return opType;
    }

    public String getArg() {
        return arg;
    }

    public String getOperand1() {
        return operand1;
    }

    public String getOperand2() {
        return operand2;
    }

    public char getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return String.format("Operation[tid %d, type %s, arg %s]", tid, opType, arg);
    }
}
