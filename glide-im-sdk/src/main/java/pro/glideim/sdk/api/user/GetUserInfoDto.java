package pro.glideim.sdk.api.user;

import java.util.List;

public class GetUserInfoDto {
    private List<Long> uid;

    public GetUserInfoDto(List<Long> uid) {
        this.uid = uid;
    }

    public List<Long> getUid() {
        return uid;
    }

    public void setUid(List<Long> uid) {
        this.uid = uid;
    }
}
