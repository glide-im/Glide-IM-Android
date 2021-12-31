package pro.glideim.sdk.ws;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.reactivex.Single;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import pro.glideim.sdk.http.RetrofitManager;
import pro.glideim.sdk.im.ConnStateListener;
import pro.glideim.sdk.utils.SLogger;

public class RetrofitWsClient implements WsClient {

    private static final String TAG = RetrofitWsClient.class.getSimpleName();
    private final List<ConnStateListener> connStateListener = new CopyOnWriteArrayList<>();
    private final String url;
    private WebSocket ws;
    private MessageListener messageListener;
    private int state = WsClient.STATE_CLOSED;
    private Throwable connectError;

    public RetrofitWsClient(String url) {
        this.url = url;
    }

    @Override
    public Single<Boolean> connect() {
        if (WsClient.STATE_CONNECTING == getState()) {
            return Single.error(new Exception("WebSocket connecting"));
        }
        Single<Boolean> booleanSingle = Single.create(emitter -> {
            try {
                onStateChange(WsClient.STATE_CONNECTING);
                connectError = null;
                ws = RetrofitManager.newWebSocket(url, new WebSocketListenerProxy());
                long l = System.currentTimeMillis();
                while (getState() == STATE_CONNECTING) {
                    if ((System.currentTimeMillis() - l) / 1000 > 10) {
                        try {
                            disconnect();
                        } catch (Exception ignored) {
                        }
                        emitter.onError(new Exception("connect timeout"));
                        return;
                    }
                }
                if (getState() != STATE_OPENED) {
                    if (connectError == null) {
                        connectError = new Exception("connect failed");
                    }
                    emitter.onError(connectError);
                } else {
                    SLogger.d(TAG, "connect success");
                    emitter.onSuccess(true);
                }
            } catch (Throwable throwable) {
                emitter.onError(throwable);
            }
        });
        return booleanSingle;
    }

    @Override
    public void removeStateListener(ConnStateListener listener) {
        this.connStateListener.remove(listener);
    }

    @Override
    public synchronized void disconnect() {
        ws.close(1000, "");
        onStateChange(WsClient.STATE_CLOSED);
    }

    @Override
    public synchronized boolean isConnected() {
        return getState() == WsClient.STATE_OPENED;
    }

    @Override
    public synchronized int getState() {
        return state;
    }

    @Override
    public synchronized void addStateListener(ConnStateListener listener) {
        this.connStateListener.add(listener);
    }

    @Override
    public synchronized boolean write(Object obj) {
        String json = RetrofitManager.toJson(obj);
        return this.ws.send(json);
    }

    @Override
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    private synchronized void onStateChange(int state) {
        if (getState() == state) {
            return;
        }
        this.state = state;
        for (ConnStateListener stateListener : connStateListener) {
            stateListener.onStateChange(state, "");
        }
    }

    private class WebSocketListenerProxy extends WebSocketListener {

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            SLogger.d(TAG, "ws closed, code=" + code + ", reason=" + reason);
            onStateChange(WsClient.STATE_CLOSED);
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {

        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            SLogger.e(TAG, t);
            if (getState() == WsClient.STATE_CONNECTING) {
                connectError = t;
            }
            if (getState() != WsClient.STATE_CLOSED) {
                onStateChange(WsClient.STATE_CLOSED);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            if (messageListener != null) {
                messageListener.onNewMessage(text);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {

        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            SLogger.d(TAG, "ws opened");
            onStateChange(WsClient.STATE_OPENED);
        }
    }
}
