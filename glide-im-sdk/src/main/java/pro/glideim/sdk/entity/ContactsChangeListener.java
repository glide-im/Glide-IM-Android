package pro.glideim.sdk.entity;

import java.util.List;

public interface ContactsChangeListener {
    void onNewContact(List<IMContacts> c);
}
