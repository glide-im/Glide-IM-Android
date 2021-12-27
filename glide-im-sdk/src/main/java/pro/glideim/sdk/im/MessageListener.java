package pro.glideim.sdk.im;

import pro.glideim.sdk.protocol.ChatMessage;

public interface MessageListener {
    void onNewMessage(ChatMessage m);
}
