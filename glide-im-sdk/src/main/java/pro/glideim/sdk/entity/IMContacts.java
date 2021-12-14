package pro.glideim.sdk.entity;

import pro.glideim.sdk.api.user.ContactsBean;

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
