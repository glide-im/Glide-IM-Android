package pro.glideim.sdk.im;

import pro.glideim.sdk.protocol.ChatMessage;

public interface IMMessageListener {
    void onNewMessage(ChatMessage message);

    void onNewContact();

    void onKickOut();

    void onNotifyNeedAuth();
}
