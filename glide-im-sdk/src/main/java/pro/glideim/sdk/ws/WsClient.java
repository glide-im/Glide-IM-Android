package pro.glideim.sdk.ws;

import io.reactivex.Single;
import pro.glideim.sdk.im.ConnStateListener;

public interface WsClient {

    int STATE_CLOSED = 1;
    int STATE_OPENED = 2;
    int STATE_CONNECTING = 3;

    Single<Boolean> connect(String url);

    void disconnect();

    boolean isConnected();

    int getState();

    boolean write(Object msg);

    void addStateListener(ConnStateListener listener);

    void setMessageListener(MessageListener listener);
}
