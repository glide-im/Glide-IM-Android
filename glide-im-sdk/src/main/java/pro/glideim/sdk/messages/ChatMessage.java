package pro.glideim.sdk.messages;

public class ChatMessage {

    public static final int STATE_INIT = -1;
    public static final int STATE_CREATED = 1;
    public static final int STATE_SRV_SENDING = 2;
    public static final int STATE_RCV_SENDING = 3;
    public static final int STATE_SRV_RECEIVED = 4;
    public static final int STATE_RCV_RECEIVED = 5;
    public static final int STATE_SRV_FAILED = 6;
    public static final int STATE_RCV_FAILED = 7;

    public static final int STATE_UPDATED = 1000;

    private long mid;
    private long cSeq;
    private long from;
    private long to;
    private int type;
    private String content;
    private long cTime;
    private int status;

    private transient int state;

    public int getState() {
        return state;
    }

    public ChatMessage setState(int state) {
        this.state = state;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

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
