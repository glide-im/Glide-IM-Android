package pro.glideim.sdk.api.msg;

public class GetChatHistoryDto {
    private long uid;
    private int page;

    public GetChatHistoryDto(long uid) {
        this.uid = uid;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
