package pro.glideim.sdk;

import io.reactivex.Single;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.user.ContactsBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.utils.SLogger;

public class IMContacts {
    public String title;
    public String avatar;
    public long id;
    public int type;

    public static IMContacts fromContactsBean(ContactsBean contactsBean) {
        IMContacts c = new IMContacts();
        c.type = contactsBean.getType();
        c.id = contactsBean.getId();
        c.title = contactsBean.getRemark();
        return c;
    }

    public Single<IMContacts> update() {
        switch (type) {
            case Constants.SESSION_TYPE_USER:
                return GlideIM.getUserInfo(id)
                        .doOnNext(this::setDetail)
                        .toList()
                        .map(userInfoBeans -> IMContacts.this);
            case Constants.SESSION_TYPE_GROUP:
                return GlideIM.getGroupInfo(id)
                        .doOnNext(this::setDetail)
                        .toList()
                        .map(userInfoBeans -> IMContacts.this);
            default:
                SLogger.d("IMContacts", "unknown contacts type " + type);
                return Single.just(this);
        }
    }

    private void setDetail(UserInfoBean userInfoBean) {
        title = userInfoBean.getNickname();
        avatar = userInfoBean.getAvatar();
    }

    private void setDetail(GroupInfoBean groupInfoBean) {
        title = groupInfoBean.getName();
        avatar = groupInfoBean.getAvatar();
    }

    @Override
    public String toString() {
        return "IMContacts{" +
                "title='" + title + '\'' +
                ", avatar='" + avatar + '\'' +
                ", id=" + id +
                ", type=" + type +
                '}';
    }
}
