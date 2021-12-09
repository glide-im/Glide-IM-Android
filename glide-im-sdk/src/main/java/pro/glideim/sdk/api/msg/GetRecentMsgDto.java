package pro.glideim.sdk.api.msg;

import java.util.List;

public class GetRecentMsgDto {
    private List<Integer> uid;

    public GetRecentMsgDto(List<Integer> uid) {
        this.uid = uid;
    }

    public List<Integer> getUid() {
        return uid;
    }

    public void setUid(List<Integer> uid) {
        this.uid = uid;
    }
}
