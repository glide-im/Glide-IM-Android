package pro.glideim.sdk.api.msg;

import java.util.List;

public class GetUserMsgDto {
    private List<Integer> uid;

    public GetUserMsgDto(List<Integer> uid) {
        this.uid = uid;
    }

    public List<Integer> getUid() {
        return uid;
    }

    public void setUid(List<Integer> uid) {
        this.uid = uid;
    }
}
