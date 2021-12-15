package pro.glideim.sdk.api.msg;

import java.util.List;

public class AckOfflineMsgDto {
    private List<Long> mid;

    public AckOfflineMsgDto(List<Long> mid) {
        this.mid = mid;
    }

    public List<Long> getMid() {
        return mid;
    }

    public void setMid(List<Long> mid) {
        this.mid = mid;
    }
}
