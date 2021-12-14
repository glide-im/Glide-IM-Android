package pro.glideim.sdk.api.msg;

public class GetGroupMsgHistoryDto {
    private long gid;
    private int page;

    public GetGroupMsgHistoryDto(long gid, int page) {
        this.gid = gid;
        this.page = page;
    }

    public long getGid() {
        return gid;
    }

    public void setGid(long gid) {
        this.gid = gid;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
