package pro.glideim.sdk.im;

import io.reactivex.Observable;
import io.reactivex.Single;
import pro.glideim.sdk.protocol.ChatMessage;
import pro.glideim.sdk.protocol.CommMessage;

public interface IMClient {
    void setConnStateListener(ConnStateListener connStateListener);

    Single<Boolean> connect(String url);

    Observable<ChatMessage> sendChatMessage(ChatMessage m);

    <T> Observable<CommMessage<T>> request(String action, Class<T> clazz, boolean isArray, Object data);
}
