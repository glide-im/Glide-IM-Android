package pro.glideim.sdk;

public interface ContactChangeListener {
    void onNewContact(IMContact c);

    void onContactUpdate(IMContact contact);

    void onContactRemove(IMContact contact);
}
