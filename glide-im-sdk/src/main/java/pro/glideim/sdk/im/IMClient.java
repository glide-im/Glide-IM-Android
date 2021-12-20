package pro.glideim.sdk.im;

import io.reactivex.Observable;
import pro.glideim.sdk.protocol.ChatMessage;
import pro.glideim.sdk.protocol.CommMessage;

public interface IMClient {
    void setConnStateListener(ConnStateListener connStateListener);

    void connect(String url, IMConnectListener l);

    Observable<ChatMessage> sendChatMessage(ChatMessage m);

    <T> Observable<CommMessage<T>> request(String action, Class<T> clazz, boolean isArray, Object data);
}
