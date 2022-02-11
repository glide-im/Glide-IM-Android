package pro.glideim.sdk.messages;

public class RecallMessage {
    private long reCallBy;
    private long mid;

    public RecallMessage(long reCallBy, long mid) {
        this.reCallBy = reCallBy;
        this.mid = mid;
    }

    public long getMid() {
        return mid;
    }

    public void setMid(long mid) {
        this.mid = mid;
    }

    public long getReCallBy() {
        return reCallBy;
    }

    public void setReCallBy(long reCallBy) {
        this.reCallBy = reCallBy;
    }
}
