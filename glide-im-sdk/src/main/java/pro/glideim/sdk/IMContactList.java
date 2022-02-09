package pro.glideim.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import pro.glideim.sdk.utils.SLogger;

public class IMContactList {

    private final TreeMap<Long, IMContact> contactsUser = new TreeMap<>();
    private final TreeMap<Long, IMGroupContact> contactsGroup = new TreeMap<>();
    private final IMAccount account;
    private final List<ContactChangeListener> contactChangeListeners = new ArrayList<>();

    public IMContactList(IMAccount account) {
        this.account = account;
    }

    public void addContactChangeListener(ContactChangeListener l) {
        this.contactChangeListeners.add(l);
    }

    public void removeContactChangeListener(ContactChangeListener l) {
        this.contactChangeListeners.remove(l);
    }

    public IMContact getUser(long uid) {
        return contactsUser.get(uid);
    }

    public IMGroupContact getGroup(long gid) {
        return contactsGroup.get(gid);
    }

    public void removeGroup(long gid) {
        IMContact contact = contactsGroup.remove(gid);
        onRemove(contact);
    }

    public void removeUser(long uid) {
        IMContact contact = contactsUser.remove(uid);
        onRemove(contact);
    }

    private void onRemove(IMContact c) {
        for (ContactChangeListener l : contactChangeListeners) {
            l.onContactRemove(c);
        }
    }

    void onUpdate(IMContact c) {
        for (ContactChangeListener l : contactChangeListeners) {
            l.onContactUpdate(c);
        }
    }

    public void addContacts(IMContact contact) {
        switch (contact.type) {
            case IMContact.TYPE_GROUP:
                if (!(contact instanceof IMGroupContact)) {
                    SLogger.e("IMContactList", new IllegalArgumentException("group contact not instance of IMGroupContact"));
                    return;
                }
                contactsGroup.put(contact.id, ((IMGroupContact) contact));
                break;
            case IMContact.TYPE_USER:
                contactsUser.put(contact.id, contact);
                break;
            default:
                SLogger.e("IMContactList", new IllegalArgumentException("unknown contact type"));
                return;
        }
        for (ContactChangeListener l : contactChangeListeners) {
            l.onNewContact(contact);
        }
    }

    public List<IMContact> getAll() {
        return new ArrayList<>(contactsUser.values());
    }
}
