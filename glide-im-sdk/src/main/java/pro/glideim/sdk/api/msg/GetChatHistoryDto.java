package pro.glideim.sdk.api.msg;

public class GetChatHistoryDto {
    private long uid;
    private long beforeMid;

    public GetChatHistoryDto(long uid, long beforeMid) {
        this.uid = uid;
        this.beforeMid = beforeMid;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getBeforeMid() {
        return beforeMid;
    }

    public void setBeforeMid(long beforeMid) {
        this.beforeMid = beforeMid;
    }
}
