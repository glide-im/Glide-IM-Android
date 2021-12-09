package pro.glideim.sdk.protocol;

public class AckRequest {
    private long mid;
    private long from;
    private long seq;

    public AckRequest(long mid, long from, long seq) {
        this.mid = mid;
        this.from = from;
        this.seq = seq;
    }

    public long getMid() {
        return mid;
    }

    public void setMid(long mid) {
        this.mid = mid;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }
}
