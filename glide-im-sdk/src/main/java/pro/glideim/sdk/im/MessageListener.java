package pro.glideim.sdk.im;

import pro.glideim.sdk.protocol.ChatMessage;
import pro.glideim.sdk.protocol.CommMessage;

public interface MessageListener {
    void onNewMessage(ChatMessage m);
    void onControlMessage(CommMessage<Object> m);
}
