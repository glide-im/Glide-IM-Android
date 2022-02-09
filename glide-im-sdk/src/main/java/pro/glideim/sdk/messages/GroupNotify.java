package pro.glideim.sdk.messages;

public class GroupNotify<T> {

    public static final int TYPE_MEMBER_ADDED = 1;
    public static final int TYPE_MEMBER_REMOVED = 2;

    private long mid;
    private long gid;
    private long type;
    private long timestamp;
    private long seq;
    private T data;

    public long getMid() {
        return mid;
    }

    public void setMid(long mid) {
        this.mid = mid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public long getGid() {
        return gid;
    }

    public void setGid(long gid) {
        this.gid = gid;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
