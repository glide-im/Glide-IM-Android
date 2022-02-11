package pro.glideim.sdk.im;

import io.reactivex.Observable;
import io.reactivex.Single;
import pro.glideim.sdk.messages.ChatMessage;
import pro.glideim.sdk.messages.CommMessage;
import pro.glideim.sdk.ws.WsClient;

public interface IMClient {
    void addConnStateListener(ConnStateListener connStateListener);

    void removeConnStateListener(ConnStateListener connStateListener);

    void setMessageListener(MessageListener listener);

    Single<Boolean> connect();

    boolean isConnected();

    Observable<ChatMessage> sendChatMessage(ChatMessage m);

    Observable<ChatMessage> sendMessage(String action, ChatMessage m);

    Observable<ChatMessage> sendGroupMessage(ChatMessage m);

    <T> Observable<CommMessage<T>> request(String action, Class<T> clazz, boolean isArray, Object data);

    void disconnect();

    boolean send(Object obj);

    WsClient getWebSocketClient();
}
