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

    void check() {
        if (client.getState() != WsClient.STATE_CLOSED || reconnecting) {
            return;
        }
        reconnecting = true;
        client.removeStateListener(this);
        SLogger.d(TAG, "reconnecting the server");
        client.connect()
                .retry(10)
                .compose(RxUtils.silentSchedulerSingle())
                .doOnSuccess(aBoolean -> {
                    reconnecting = false;
                    client.addStateListener(this);
                    SLogger.d(TAG, "reconnect server success");
                })
                .doOnError(e -> {
                    SLogger.d(TAG, "reconnect server failed: " + e.getMessage());
                })
                .subscribe(new SilentObserver<>());
    }
}
