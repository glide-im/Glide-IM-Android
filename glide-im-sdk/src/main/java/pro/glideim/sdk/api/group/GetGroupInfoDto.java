package pro.glideim.sdk.api.group;

import java.util.List;

public class GetGroupInfoDto {
    private List<Long> gid;

    public GetGroupInfoDto(List<Long> gid) {
        this.gid = gid;
    }

    public List<Long> getGid() {
        return gid;
    }

    public void setGid(List<Long> gid) {
        this.gid = gid;
    }
}
