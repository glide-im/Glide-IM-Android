package pro.glideim.sdk;

import java.util.List;

import pro.glideim.sdk.entity.IMContacts;

public interface ContactsChangeListener {
    void onUpdate(List<IMContacts> c);
}
