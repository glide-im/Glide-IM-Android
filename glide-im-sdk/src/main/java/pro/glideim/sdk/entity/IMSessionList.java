package pro.glideim.sdk.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMSessionList {

    private final Map<IdTag, IMSession> sessionMap = new HashMap<>();
    private SessionUpdateListener sessionUpdateListener;

    public IMSession getSession(long id, int type) {
        IdTag idTag = IdTag.get(type, id);
        return sessionMap.get(idTag);
    }

    public void setSessionUpdateListener(SessionUpdateListener sessionUpdateListener) {
        this.sessionUpdateListener = sessionUpdateListener;
    }

    public IMSession getSession(int type, long id) {
        IdTag idTag = IdTag.get(type, id);
        return sessionMap.get(idTag);
    }

    public boolean containSession(int type, long id) {
        return sessionMap.containsKey(IdTag.get(type, id));
    }

    public void addSession(List<IMSession> s) {
        for (IMSession ses : s) {
            IdTag tag = IdTag.get(ses.type, ses.to);
            sessionMap.put(tag, ses);
        }
    }

    public void addMessage(IMMessage message) {
        IdTag idTag = IdTag.get(message.getTargetType(), message.getTargetId());
        if (!sessionMap.containsKey(idTag)) {
            sessionMap.put(idTag, IMSession.fromIMMessage(message));
        }
        sessionMap.get(idTag).latestMessage.add(message);
    }

    public void setSessionRecentMessages(List<IMMessage> messages) {
        Map<IdTag, List<IMMessage>> m = new HashMap<>();
        for (IMMessage message : messages) {
            IdTag idTag = IdTag.get(message.getTargetType(), message.getTargetId());
            if (!m.containsKey(idTag)) {
                m.put(idTag, new ArrayList<>());
            }
            m.get(idTag).add(message);
        }
        m.forEach((idTag, messages1) -> {
            if (sessionMap.containsKey(idTag)) {
                sessionMap.get(idTag).setLatestMessage(messages1);
            } else {
                IMSession newSession = IMSession.create(idTag.getId(), idTag.getType());
                newSession.initTargetInfo();
                newSession.setLatestMessage(messages1);
                sessionMap.put(idTag, newSession);
            }
        });
    }

    public IMSession[] getAll() {
        IMSession[] a = new IMSession[]{};
        a = sessionMap.values().toArray(a);
        return a;
    }
}
