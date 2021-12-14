package pro.glideim.sdk.api.msg;

public class GroupMessageStateBean {
    private Long gid;
    private long lastMID;
    private long lastSeq;
    private long lastMsgAt;

    public Long getGid() {
        return gid;
    }

    public void setGid(Long gid) {
        this.gid = gid;
    }

    public long getLastMID() {
        return lastMID;
    }

    public void setLastMID(long lastMID) {
        this.lastMID = lastMID;
    }

    public long getLastSeq() {
        return lastSeq;
    }

    public void setLastSeq(long lastSeq) {
        this.lastSeq = lastSeq;
    }

    public long getLastMsgAt() {
        return lastMsgAt;
    }

    public void setLastMsgAt(long lastMsgAt) {
        this.lastMsgAt = lastMsgAt;
    }
}
