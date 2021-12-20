package pro.glideim.sdk.im;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pro.glideim.sdk.http.RetrofitManager;

import java.io.EOFException;
import java.util.concurrent.ExecutionException;

public class WsClient {

    private WebSocket ws;
    private WebSocketListener listener;

    public void setListener(WebSocketListener listener) {
        this.listener = listener;
    }

    public void connect(String url) throws ExecutionException, InterruptedException {
        ws = RetrofitManager.newWebSocket(url, new WebSocketListenerProxy());
    }

    public void disconnect() {
        ws.close(1000, "");
    }

    public boolean sendMessage(Object obj) {
        String json = RetrofitManager.toJson(obj);
        System.out.println("WsClient.sendMessage:" + json);
        return this.ws.send(json);
    }

    private class WebSocketListenerProxy extends WebSocketListener {

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            System.out.println("WsClient.onClosed");
            if (listener != null) {
                listener.onClosed(webSocket, code, reason);
            }
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            System.out.println("WsClient.onClosing");
            if (listener != null) {
                listener.onClosing(webSocket, code, reason);
            }
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            if (t instanceof EOFException) {
                this.onClosed(webSocket, -1, "EOF, closed");
                return;
            }

            System.out.println("WsClient.onFailure " + t.getMessage());
            if (listener != null) {
                listener.onFailure(webSocket, t, response);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            if (listener != null) {
                listener.onMessage(webSocket, text);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            System.out.println(bytes);
            if (listener != null) {
                listener.onMessage(webSocket, bytes);
            }
        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            System.out.println("WsClient.onOpen");
            if (listener != null) {
                listener.onOpen(webSocket, response);
            }
        }
    }
}
