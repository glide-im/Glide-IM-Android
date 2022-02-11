package pro.glideim.sdk.ws;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private int lastNotifyState = 0;
    private final ExecutorService executors = Executors.newFixedThreadPool(6);

    public RetrofitWsClient(String url) {
        this.url = url;
    }

    @Override
    public Single<Boolean> connect() {
        if (WsClient.STATE_CONNECTING == getState()) {
            return Single.error(new IllegalStateException("WebSocket in connecting"));
        }
        return Single.create(emitter -> {
            onStateChange(WsClient.STATE_CONNECTING);
            ws = RetrofitManager.newWebSocket(url, new WebSocketListenerProxy((success, t) -> {
                if (emitter.isDisposed()) {
                    return;
                }
                if (success) {
                    emitter.onSuccess(true);
                } else {
                    try {
                        ws.close(1000, "");
                    } catch (Throwable ignored) {
                    }
                    emitter.onError(t);
                }
            }));
        });
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
        if (lastNotifyState == state) {
            return;
        }
        lastNotifyState = state;
        for (ConnStateListener stateListener : connStateListener) {
            stateListener.onStateChange(state, "");
        }
    }


    private interface ConnectListener {
        void finish(boolean success, Throwable t);
    }

    private class WebSocketListenerProxy extends WebSocketListener {

        private ConnectListener connectListener;

        public WebSocketListenerProxy(ConnectListener connectListener) {
            this.connectListener = connectListener;
        }

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
            if (returnConnectResult(false, t) && getState() == WsClient.STATE_CONNECTING) {
                state = WsClient.STATE_CLOSED;
                return;
            }
            SLogger.e(TAG, t);
            if (getState() != WsClient.STATE_CLOSED) {
                onStateChange(WsClient.STATE_CLOSED);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            if (messageListener != null) {
                executors.submit(() -> messageListener.onNewMessage(text));
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {

        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            SLogger.d(TAG, "ws opened");
            onStateChange(WsClient.STATE_OPENED);
            returnConnectResult(true, null);
        }

        private boolean returnConnectResult(boolean ok, Throwable t) {
            if (connectListener != null) {
                connectListener.finish(ok, t);
                connectListener = null;
                return true;
            }
            return false;
        }
    }
}
