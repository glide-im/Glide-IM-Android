package pro.glideim.sdk.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class IMSessionList {

    private final TreeMap<SessionTag, IMSession> sessionMap = new TreeMap<>();
    private final List<IMSession> sessionList = new ArrayList<>();

    private SessionUpdateListener sessionUpdateListener;

    public void setSessionUpdateListener(SessionUpdateListener sessionUpdateListener) {
        this.sessionUpdateListener = sessionUpdateListener;
    }

    public IMSession getSession(int type, long id) {
        SessionTag sessionTag = SessionTag.get(type, id);
        return sessionMap.get(sessionTag);
    }

    public boolean containSession(int type, long id) {
        return sessionMap.containsKey(SessionTag.get(type, id));
    }

    public void updateSession(IMSession ses) {
        SessionTag tag = SessionTag.get(ses.type, ses.to);
        tag.updateAt = ses.updateAt;
        if (sessionMap.containsKey(tag)) {
            sessionList.remove(sessionMap.remove(tag));
        }
        ses.setIMSessionList(this);
        sessionMap.put(tag, ses);
        sessionList.add(ses);
    }

    public void updateSession(List<IMSession> s) {
        for (IMSession ses : s) {
            updateSession(ses);
        }
    }

    public void addMessage(IMMessage message) {
        SessionTag sessionTag = SessionTag.get(message.getTargetType(), message.getTargetId());
        if (!sessionMap.containsKey(sessionTag)) {
            sessionMap.put(sessionTag, IMSession.fromIMMessage(message));
        }
        sessionTag.updateAt = message.getSendAt();
        sessionMap.get(sessionTag).addMessage(message);
    }

    public void setSessionRecentMessages(List<IMMessage> messages) {
        Map<SessionTag, List<IMMessage>> m = new HashMap<>();
        for (IMMessage message : messages) {
            SessionTag sessionTag = SessionTag.get(message.getTargetType(), message.getTargetId());
            if (!m.containsKey(sessionTag)) {
                m.put(sessionTag, new ArrayList<>());
            }
            m.get(sessionTag).add(message);
        }
        m.forEach((sessionTag, messages1) -> {
            IMSession session = sessionMap.get(sessionTag);
            if (session == null) {
                session = IMSession.create(sessionTag.getId(), sessionTag.getType());
                session.initTargetInfo();
                session.setIMSessionList(this);
                sessionMap.put(sessionTag, session);
            }
            session.addMessages(messages1);
        });
    }

    public List<IMSession> getAll() {
        return sessionList;
    }

    private static class SessionTag implements Comparable<SessionTag> {
        private static final Map<String, SessionTag> temp = new HashMap<>();
        int type;
        long id;
        private long updateAt = 0;

        private SessionTag(int type, long id) {
            this.type = type;
            this.id = id;
        }

        public static synchronized SessionTag get(int type, long id) {
            String tag = type + "@" + id;
            if (temp.containsKey(tag)) {
                return temp.get(tag);
            }
            SessionTag r = new SessionTag(type, id);
            temp.put(tag, r);
            return r;
        }

        public int getType() {
            return type;
        }

        public long getId() {
            return id;
        }

        @NonNull
        @Override
        public String toString() {
            return "SessionTag{" +
                    "type=" + type +
                    ", id=" + id +
                    ", updateAt=" + updateAt +
                    '}';
        }

        @Override
        public int compareTo(SessionTag o) {
            if (this.updateAt == o.updateAt) {
                return 0;
            }
            long l = this.updateAt - o.updateAt;
            return (int) l;
        }


    }
}
