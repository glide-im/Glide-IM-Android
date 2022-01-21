package pro.glideim.sdk;

import pro.glideim.sdk.push.NewContactsMessage;

public interface IMMessageListener {
    void onNotify(String msg);

    void onNewMessage(IMMessage message);

    void onNewContact(NewContactsMessage c);

    void onKickOut();

    void onTokenInvalid();
}
