package pro.glideim.sdk.api.msg;

import java.util.List;

public class RecentMsgBean {
    private long uid;
    private List<MessageBean> messages;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public List<MessageBean> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageBean> messages) {
        this.messages = messages;
    }
}
