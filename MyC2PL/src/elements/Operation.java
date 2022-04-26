package elements;

public class Operation {
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

    public Operation(int tid, int opType, String arg) {
        this.tid = tid;
        this.opType = opType;
        this.arg = arg;
    }

    public Operation(int tid, int opType) {
        this.tid = tid;
        this.opType = opType;
    }

    public Operation(int tid, int opType, String arg, String operand1, String operand2, char operator) {
        this.tid = tid;
        this.opType = opType;
        this.arg = arg;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operator = operator;
    }

    public int getTid() {
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
}
