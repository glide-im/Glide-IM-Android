package pro.glideim.sdk.api.group;

public class JoinGroupDto {
    private Long gid;

    public JoinGroupDto(Long gid) {
        this.gid = gid;
    }

    public Long getGid() {
        return gid;
    }

    public void setGid(Long gid) {
        this.gid = gid;
    }
}
