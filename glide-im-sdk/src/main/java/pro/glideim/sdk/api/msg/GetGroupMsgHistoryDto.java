package pro.glideim.sdk.api.msg;

public class GetGroupMsgHistoryDto {
    private long gid;
    private long beforeSeq;

    public GetGroupMsgHistoryDto(long gid) {
        this.gid = gid;
    }

    public GetGroupMsgHistoryDto(long gid, long beforeSeq) {
        this.gid = gid;
        this.beforeSeq = beforeSeq;
    }

    public long getGid() {
        return gid;
    }

    public void setGid(long gid) {
        this.gid = gid;
    }

    public long getBeforeSeq() {
        return beforeSeq;
    }

    public void setBeforeSeq(long beforeSeq) {
        this.beforeSeq = beforeSeq;
    }
}
