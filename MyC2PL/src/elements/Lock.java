package elements;

import java.io.Serializable;

public class Lock implements Serializable {
    private int sid;
    private int tid;
    public static final int readType = 0;
    public static final int writeType = 1;
    private String lockItem;
    private int lockType;
    private static final long serialVersionUID = 22L;

    public Lock(int sid, int tid, String lockItem, int lockType) {
        this.sid = sid;
        this.tid = tid;
        this.lockItem = lockItem;
        this.lockType = lockType;
    }

    public int getSID() {
        return sid;
    }

    public int getTID() {
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

    @Override
    public String toString() {
        return String.format("Lock[tid %d, type %s, arg %s]", tid, lockType, lockItem);
    }
}
