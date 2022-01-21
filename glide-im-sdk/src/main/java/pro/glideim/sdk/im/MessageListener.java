package pro.glideim.sdk.im;

import pro.glideim.sdk.messages.ChatMessage;
import pro.glideim.sdk.messages.CommMessage;
import pro.glideim.sdk.messages.GroupMessage;

public interface MessageListener {
    void onNewMessage(ChatMessage m);
    void onGroupMessage(GroupMessage m);
    void onControlMessage(CommMessage<Object> m);
}
