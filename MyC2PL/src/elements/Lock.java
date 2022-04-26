package elements;

public class Lock {
    private int tid;
    public static final int readType = 0;
    public static final int writeType = 1;
    private String lockItem;
    private int lockType;

    public Lock(int tid, String lockItem, int lockType) {
        this.tid = tid;
        this.lockItem = lockItem;
        this.lockType = lockType;
    }

    public int getTid() {
        return tid;
    }

    public String getItem() {
        return lockItem;
    }

    public int getType() {
        return lockType;
    }

    public void updateType() {
        this.lockType = writeType;
    }
}
