package pro.glideim.sdk.api.msg;

public class GetGroupMessageStateDto {
    private Long gid;


    public GetGroupMessageStateDto(Long gid) {
        this.gid = gid;
    }

    public Long getGid() {
        return gid;
    }

    public void setGid(Long gid) {
        this.gid = gid;
    }
}
