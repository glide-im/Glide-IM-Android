package pro.glideim.sdk.api.group;

import java.util.List;

public class RemoveMemberDto {
    private long gid;
    private List<Long> uid;

    public RemoveMemberDto(long gid, List<Long> uid) {
        this.gid = gid;
        this.uid = uid;
    }

    public long getGid() {
        return gid;
    }

    public void setGid(long gid) {
        this.gid = gid;
    }

    public List<Long> getUid() {
        return uid;
    }

    public void setUid(List<Long> uid) {
        this.uid = uid;
    }
}
