package pro.glideim.sdk.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import io.reactivex.Observable;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.user.ProfileBean;
import pro.glideim.sdk.api.user.UserApi;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.utils.RxUtils;

public class Account {

    private final IMSessionList sessionList = new IMSessionList();
    private final TreeMap<String, IMContacts> contactsMap = new TreeMap<>();
    public long uid;
    public ContactsChangeListener contactsChangeListener;
    private ProfileBean profileBean;

    public List<IMContacts> updateContacts(List<UserInfoBean> userInfoBeans) {
        List<IMContacts> res = new ArrayList<>();
        for (UserInfoBean userInfoBean : userInfoBeans) {
            IMContacts c = contactsMap.get(1 + "_" + userInfoBean.getUid());
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

    public IMSessionList getIMSessionList() {
        return sessionList;
    }

    public Observable<List<IMSession>> getSessions() {
        return sessionList.getSessionList();
    }

    public List<IMContacts> updateContactsGroup(List<GroupInfoBean> groupInfoBeans) {
        List<IMContacts> res = new ArrayList<>();
        for (GroupInfoBean groupInfoBean : groupInfoBeans) {
            IMContacts c = contactsMap.get(2 + "_" + groupInfoBean.getGid());
            if (c == null) {
                continue;
            }
            c.title = groupInfoBean.getName();
            c.avatar = groupInfoBean.getAvatar();
            c.id = groupInfoBean.getGid();
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

    public ProfileBean getProfile() {
        return profileBean;
    }

    public Observable<ProfileBean> initUserProfile() {
        return UserApi.API.myProfile()
                .map(RxUtils.bodyConverter())
                .doOnNext(profileBean -> Account.this.profileBean = profileBean);
    }

    public void addContacts(IMContacts c) {
        contactsMap.put(c.type + "_" + c.id, c);
    }

    public List<IMContacts> getContacts() {
        return new ArrayList<>(contactsMap.values());
    }

    public List<Long> getContactsGroup() {
        List<Long> g = new ArrayList<>();
        for (IMContacts contact : getContacts()) {
            if (contact.type == 2) {
                g.add(contact.id);
            }
        }
        return g;
    }
}
