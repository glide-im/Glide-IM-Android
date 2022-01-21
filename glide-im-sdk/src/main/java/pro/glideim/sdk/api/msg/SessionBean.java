package pro.glideim.sdk.api.msg;

public class SessionBean {
    private long uid1;
    private long uid2;
    private long lastMid;
    private long updateAt;
    private long createAt;

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public long getUid1() {
        return uid1;
    }

    public void setUid1(long uid1) {
        this.uid1 = uid1;
    }

    public long getUid2() {
        return uid2;
    }

    public void setUid2(long uid2) {
        this.uid2 = uid2;
    }

    public long getLastMid() {
        return lastMid;
    }

    public void setLastMid(long lastMid) {
        this.lastMid = lastMid;
    }

    public long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(long updateAt) {
        this.updateAt = updateAt;
    }
}
