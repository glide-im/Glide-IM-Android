package pro.glideim.sdk;

import io.reactivex.Single;
import pro.glideim.sdk.api.user.ContactsBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.utils.SLogger;

public class IMContact {

    public static final int TYPE_GROUP = 2;
    public static final int TYPE_USER = 1;

    public String title;
    public String avatar;
    public long id;
    public int type;
    private IMAccount account;

    public static IMContact fromContactsBean(ContactsBean contactsBean, IMContactList contactList, IMAccount account) {
        if (contactsBean.getType() == TYPE_GROUP) {
            return new IMGroupContact(account, contactList,contactsBean);
        }
        IMContact c = new IMContact();
        c.type = contactsBean.getType();
        c.id = contactsBean.getId();
        c.title = contactsBean.getRemark();
        c.account = account;
        return c;
    }

    public Single<? extends IMContact> update() {
        if (type == Constants.SESSION_TYPE_USER) {
            return GlideIM.getUserInfo(id)
                    .doOnSuccess(this::setDetail)
                    .map(userInfoBeans -> IMContact.this);
        }
        SLogger.d("IMContacts", "unknown contacts type " + type);
        return Single.just(this);
    }

    private void setDetail(UserInfoBean userInfoBean) {
        title = userInfoBean.getNickname();
        avatar = userInfoBean.getAvatar();
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
