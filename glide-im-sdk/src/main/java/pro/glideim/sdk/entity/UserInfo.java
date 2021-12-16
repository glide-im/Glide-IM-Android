package pro.glideim.sdk.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.user.UserInfoBean;

public class UserInfo {

    private final Map<IdTag, IMContacts> contactsMap = new HashMap<>();

    private final Map<IdTag, List<IMMessage>> messages = new HashMap<>();

    private final IMSessionList sessionList = new IMSessionList();

    public long uid;
    public String avatar = "";
    public String nickname = "-";
    public String token;
    public ContactsChangeListener contactsChangeListener;

    public void getContacts() {

    }

    public IMSessionList getSessions() {
        return sessionList;
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

    public Iterable<IdTag> getContactsIdList() {
        return contactsMap.keySet();
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
