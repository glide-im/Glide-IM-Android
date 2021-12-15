package pro.glideim.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.entity.IMContacts;
import pro.glideim.sdk.entity.IMMessage;
import pro.glideim.sdk.entity.IMSession;
import pro.glideim.sdk.entity.IdTag;

public class UserInfo {

    private final Map<IdTag, IMSession> sessionMap = new HashMap<>();
    private final Map<IdTag, IMContacts> contactsMap = new HashMap<>();

    private final Map<IdTag, List<IMMessage>> messages = new HashMap<>();

    long uid;
    String avatar = "";
    String nickname = "-";
    String token;
    SessionUpdateListener sessionUpdateListener;
    ContactsChangeListener contactsChangeListener;

    public void getContacts() {

    }

    public IMSession getSession(long id, int type) {
        IdTag idTag = IdTag.get(type, id);
        return sessionMap.get(idTag);
    }

    public List<IMContacts> updateContacts(List<UserInfoBean> userInfoBeans) {
        List<IMContacts> res = new ArrayList<>();
        for (UserInfoBean userInfoBean : userInfoBeans) {
            IdTag idTag = IdTag.get(1, userInfoBean.getUid());
            IMContacts c = contactsMap.get(idTag);
            if (c == null) {
                continue;
            }
            c.title = userInfoBean.getNickname();
            c.avatar = userInfoBean.getAvatar();
            c.id = userInfoBean.getUid();
            c.type = 1;
            res.add(c);
        }
        return res;
    }

    public List<IMContacts> updateContactsGroup(List<GroupInfoBean> userInfoBeans) {
        List<IMContacts> res = new ArrayList<>();
        for (GroupInfoBean userInfoBean : userInfoBeans) {
            IdTag idTag = IdTag.get(2, userInfoBean.getGid());
            IMContacts c = contactsMap.get(idTag);
            if (c == null) {
                continue;
            }
            c.title = userInfoBean.getName();
            c.avatar = userInfoBean.getAvatar();
            c.id = userInfoBean.getGid();
            c.type = 2;
            res.add(c);
        }
        return res;
    }

    public void addContacts(List<IMContacts> contacts) {
        for (IMContacts c : contacts) {
            addContacts(c);
        }
    }

    public void addContacts(IMContacts c) {
        IdTag tag = IdTag.get(c.type, c.id);
        contactsMap.put(tag, c);
    }

    public void addSession(List<IMSession> s) {
        for (IMSession ses : s) {
            IdTag tag = IdTag.get(ses.type, ses.to);
            sessionMap.put(tag, ses);
        }
    }

    public void addMessage(IMMessage message) {

    }

    public void setRecentMessages(List<IMMessage> messages) {
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

    public Iterable<IdTag> getContactsIdList() {
        return contactsMap.keySet();
    }

    public IMSession[] getAllSessions() {
        IMSession[] a = new IMSession[]{};
        sessionMap.values().toArray(a);
        return a;
    }

    public List<Long> getContactsGroup() {
        List<Long> g = new ArrayList<>();
        for (IdTag idTag : contactsMap.keySet()) {
            if (idTag.getType() == 2) {
                g.add(idTag.getId());
            }
        }
        return g;
    }
}
