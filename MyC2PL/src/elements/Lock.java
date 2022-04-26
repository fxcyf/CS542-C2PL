package elements;

public class Lock {
    private int tid;
    private String lockItem;
    private String lockType;

    public Lock(int tid, String lockItem, String lockType) {
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

    public String getType() {
        return lockType;
    }

    public void updateType() {
        lockType = "write";
    }
}
