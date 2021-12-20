package pro.glideim.sdk.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class IMSessionMessage {

    private final TreeMap<Long, IMMessage> messageTreeMap = new TreeMap<>();
    private final IMSession session;
    private MessageChangeListener messageChangeListener;

    IMSessionMessage(IMSession s) {
        this.session = s;
    }

    void addMessage(IMMessage msg) {
        // TODO cache msg to file
        long mid = msg.getMid();
        messageTreeMap.put(mid, msg);
        long last = messageTreeMap.lastKey();
        if (last == mid) {
            session.lastMsg = msg.getContent();
            session.lastMsgId = mid;
            session.lastMsgSender = msg.getFrom();
            session.updateAt = msg.getSendAt();
        }
        onChange(mid);
    }

    void addMessages(List<IMMessage> msg) {
        for (IMMessage m : msg) {
            addMessage(m);
        }
    }

    private void onChange(long mid) {
        if (messageChangeListener != null) {
            messageChangeListener.onChange(mid);
        }
    }

    public void setOnChangeListener(MessageChangeListener l) {
        this.messageChangeListener = l;
    }

    public List<IMMessage> getMessages(long beforeMid, int maxLen) {
        List<IMMessage> ret = new ArrayList<>();
        if (messageTreeMap.isEmpty()) {
            return ret;
        }
        Long mid = beforeMid;
        if (beforeMid == 0) {
            messageTreeMap.lastKey();
        } else {
            messageTreeMap.lowerKey(beforeMid);
        }
        int count = maxLen;
        while (mid != null && count > 0) {
            IMMessage m = messageTreeMap.get(mid);
            ret.add(0, m);
            mid = messageTreeMap.lowerKey(mid);
            count--;
        }
        return ret;
    }

    public List<IMMessage> getLatest() {
        return getMessages(0, 10);
    }
}
