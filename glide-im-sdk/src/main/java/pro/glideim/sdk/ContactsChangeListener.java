package pro.glideim.sdk;

import java.util.List;

public interface ContactsChangeListener {
    void onNewContact(List<IMContacts> c);
    void onContactUpdate(IMContacts contacts);
}
