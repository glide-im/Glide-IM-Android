package pro.glideim.sdk.im;

import pro.glideim.sdk.protocol.ChatMessage;
import pro.glideim.sdk.protocol.CommMessage;
import pro.glideim.sdk.protocol.GroupMessage;

public interface MessageListener {
    void onNewMessage(ChatMessage m);
    void onGroupMessage(GroupMessage m);
    void onControlMessage(CommMessage<Object> m);
}
