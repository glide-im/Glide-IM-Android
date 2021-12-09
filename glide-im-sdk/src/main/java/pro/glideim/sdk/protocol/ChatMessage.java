package pro.glideim.sdk.protocol;

public class ChatMessage {
    private long mid;
    private long cSeq;
    private long from;
    private long to;
    private int type;
    private String content;
    private long cTime;

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getMid() {
        return mid;
    }

    public void setMid(long mid) {
        this.mid = mid;
    }

    public long getcSeq() {
        return cSeq;
    }

    public void setcSeq(long cSeq) {
        this.cSeq = cSeq;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getcTime() {
        return cTime;
    }

    public void setcTime(long cTime) {
        this.cTime = cTime;
    }
}
