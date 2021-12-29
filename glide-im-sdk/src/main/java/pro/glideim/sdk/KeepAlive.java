package pro.glideim.sdk;

import pro.glideim.sdk.im.ConnStateListener;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;
import pro.glideim.sdk.ws.WsClient;

public class KeepAlive implements ConnStateListener {

    private static final String TAG = KeepAlive.class.getSimpleName();

    private final WsClient client;

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
        if (client.getState() != WsClient.STATE_CLOSED) {
            return;
        }
        SLogger.d(TAG, "reconnecting the server");
        client.connect()
                .retry(10)
                .compose(RxUtils.silentSchedulerSingle())
                .doOnSuccess(aBoolean -> {
                    GlideIM.authWs().compose(RxUtils.silentScheduler())
                            .subscribe(new SilentObserver<>());
                })
                .doOnError(e -> {
                    SLogger.d(TAG, "reconnect server failed");
                    SLogger.e(TAG, e);
                })
                .subscribe(new SilentObserver<>());
    }
}
