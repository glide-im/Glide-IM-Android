package pro.glideim.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.user.ContactsBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.entity.IMContacts;
import pro.glideim.sdk.entity.IMSession;
import pro.glideim.sdk.entity.IdTag;

public class UserInfo {

    private final List<IMContacts> contacts = new ArrayList<>();

    private final List<IMSession> sessions = new ArrayList<>();


    private final Map<IdTag, IMSession> sessionMap = new HashMap<>();
    private final Map<IdTag, IMContacts> contactsMap = new HashMap<>();

    long uid;
    String avatar = "";
    String nickname = "-";
    String token;
    SessionUpdateListener sessionUpdateListener;
    ContactsChangeListener contactsChangeListener;

    public void getContacts() {

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
            IdTag idTag = IdTag.get(1, userInfoBean.getGid());
            IMContacts c = contactsMap.get(idTag);
            if (c == null) {
                continue;
            }
            c.title = userInfoBean.getName();
            c.avatar = userInfoBean.getAvatar();
            c.id = userInfoBean.getGid();
            c.type = 1;
            res.add(c);
        }
        return res;
    }

    public void addContacts(List<ContactsBean> contacts) {
        for (ContactsBean c : contacts) {
            IdTag tag = IdTag.get(c.getType(), c.getId());
            contactsMap.put(tag, IMContacts.fromContactsBean(c));
        }
    }

    public void addSession(List<IMSession> s) {
        for (IMSession ses : s) {
            IdTag tag = IdTag.get(ses.type, ses.to);
            sessionMap.put(tag, ses);
        }
    }

    public Iterable<IdTag> getContactsIdList() {
        return contactsMap.keySet();
    }

    public void getGroupSession() {

    }

    public IMSession[] getAllSessions() {
        IMSession[] a = new IMSession[]{};
        sessionMap.values().toArray(a);
        return a;
    }

    public List<Long> getContactsGroup() {
        List<Long> g = new ArrayList<>();
        for (IMContacts contact : contacts) {
            if (contact.type == 2) {
                g.add(contact.id);
            }
        }
        return g;
    }
}
