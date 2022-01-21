package pro.glideim.sdk.api.group;

public class GetGroupMemberDto {
    private long gid;

    public GetGroupMemberDto(long gid) {
        this.gid = gid;
    }

    public long getGid() {
        return gid;
    }

    public void setGid(long gid) {
        this.gid = gid;
    }
}
