package pro.glideim.sdk.api.group;

import java.util.List;

public class RemoveMemberDto {
    private List<Long> uid;

    public RemoveMemberDto(List<Long> uid) {
        this.uid = uid;
    }

    public List<Long> getUid() {
        return uid;
    }

    public void setUid(List<Long> uid) {
        this.uid = uid;
    }
}
