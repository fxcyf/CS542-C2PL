package elements;

public class Operation {
    private int tid;
    private String opType;
    private String arg;
    private String operand1;
    private String operand2;
    private char operator;

    public Operation(int tid, String opType, String arg) {
        this.tid = tid;
        this.opType = opType;
        this.arg = arg;
    }

    public Operation(int tid, String opType) {
        this.tid = tid;
        this.opType = opType;
    }

    public Operation(int tid, String opType, String arg, String operand1, String operand2, char operator) {
        this.tid = tid;
        this.opType = opType;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operator = operator;
    }

    public int getTid() {
        return tid;
    }

    public String getType() {
        return opType;
    }

    public String getArg() {
        return arg;
    }

}
