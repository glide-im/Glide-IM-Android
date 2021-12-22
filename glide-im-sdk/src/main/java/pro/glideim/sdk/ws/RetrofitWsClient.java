package pro.glideim.sdk.ws;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;

import io.reactivex.Single;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import pro.glideim.sdk.http.RetrofitManager;
import pro.glideim.sdk.im.ConnStateListener;

public class RetrofitWsClient implements WsClient {

    private WebSocket ws;
    private ConnStateListener connStateListener;
    private MessageListener messageListener;
    private int state = WsClient.STATE_CLOSED;

    @Override
    public Single<Boolean> connect(String url) {
        onStateChange(WsClient.STATE_CONNECTING);
        return Single.create(emitter -> {
            ws = RetrofitManager.newWebSocket(url, new WebSocketListenerProxy());
        });
    }

    @Override
    public void disconnect() {
        ws.close(1000, "");
    }

    @Override
    public boolean isConnected() {
        return state == WsClient.STATE_OPENED;
    }

    @Override
    public void addStateListener(ConnStateListener listener) {
        this.connStateListener = listener;
    }

    @Override
    public boolean write(Object obj) {
        String json = RetrofitManager.toJson(obj);
        System.out.println("WsClient.sendMessage:" + json);
        return this.ws.send(json);
    }

    @Override
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    private void onStateChange(int state) {
        if (connStateListener != null) {
            connStateListener.onStateChange(state, "");
        }
        this.state = state;
    }

    private class WebSocketListenerProxy extends WebSocketListener {

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            System.out.println("WsClient.onClosed");
            onStateChange(WsClient.STATE_CLOSED);
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            System.out.println("WsClient.onClosing");

        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            if (t instanceof EOFException) {
                this.onClosed(webSocket, -1, "EOF, closed");
                return;
            }
            System.out.println("WsClient.onFailure " + t.getMessage());
            if (state == WsClient.STATE_CONNECTING) {
                onStateChange(WsClient.STATE_CLOSED);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            System.out.println("WsClient.onMessage " + text);
            if (messageListener != null) {
                messageListener.onNewMessage(text);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            System.out.println("WsClient.onMessage bytes=" + bytes);

        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            System.out.println("WsClient.onOpen");
            onStateChange(WsClient.STATE_OPENED);
        }
    }
}
