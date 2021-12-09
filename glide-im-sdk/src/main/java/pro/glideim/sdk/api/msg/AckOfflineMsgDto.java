package pro.glideim.sdk.api.msg;

import java.util.List;

public class AckOfflineMsgDto {
    private List<Integer> mid;

    public List<Integer> getMid() {
        return mid;
    }

    public void setMid(List<Integer> mid) {
        this.mid = mid;
    }
}
