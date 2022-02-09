package pro.glideim.sdk.messages;

import java.util.List;

public class GroupNotifyMemberChanges {
    private List<Long> uid;

    public List<Long> getUid() {
        return uid;
    }

    public void setUid(List<Long> uid) {
        this.uid = uid;
    }
}
