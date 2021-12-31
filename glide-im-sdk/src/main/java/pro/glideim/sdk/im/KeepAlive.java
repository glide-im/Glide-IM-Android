package pro.glideim.sdk.im;

import pro.glideim.sdk.SilentObserver;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;
import pro.glideim.sdk.ws.WsClient;

public class KeepAlive implements ConnStateListener {

    private static final String TAG = KeepAlive.class.getSimpleName();

    private final WsClient client;
    private boolean reconnecting = false;

    private KeepAlive(WsClient client) {
        this.client = client;
        this.client.addStateListener(this);
    }

    public static KeepAlive create(WsClient client) {
        KeepAlive keepAlive = new KeepAlive(client);
        client.addStateListener(keepAlive);
        return keepAlive;
    }

    @Override
    public void onStateChange(int state, String msg) {
        check();
    }

    synchronized void check() {
        if (client.getState() != WsClient.STATE_CLOSED || reconnecting) {
            return;
        }
        reconnecting = true;
        SLogger.d(TAG, "reconnecting the server");
        client.connect()
                .retry((integer, throwable) -> {
                    SLogger.d(TAG, "retry connect to server, times:" + integer);
                    return true;
                })
                .compose(RxUtils.silentSchedulerSingle())
                .doOnSuccess(aBoolean -> {
                    reconnecting = false;
                    SLogger.d(TAG, "reconnect server success");
                })
                .doOnError(e -> {
                    SLogger.d(TAG, "reconnect server failed: " + e.getMessage());
                })
                .subscribe(new SilentObserver<>());
    }
}
