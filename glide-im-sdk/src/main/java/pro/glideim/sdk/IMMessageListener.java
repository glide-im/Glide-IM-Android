package pro.glideim.sdk;

public interface IMMessageListener {
    void onNotify(String msg);

    void onNewMessage(IMMessage message);

    void onNewContact();

    void onKickOut();

    void onTokenInvalid();
}
