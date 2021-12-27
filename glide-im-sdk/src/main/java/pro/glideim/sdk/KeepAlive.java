package pro.glideim.sdk;

import pro.glideim.sdk.im.ConnStateListener;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;
import pro.glideim.sdk.ws.WsClient;

public class KeepAlive implements ConnStateListener {

    private static final String TAG = KeepAlive.class.getSimpleName();

    private final WsClient client;
    private final String wsUrl;

    KeepAlive(WsClient client, String wsUrl) {
        this.client = client;
        this.wsUrl = wsUrl;
        this.client.addStateListener(this);
    }

    @Override
    public void onStateChange(int state, String msg) {
        if (state == WsClient.STATE_CLOSED) {
            check();
        }
    }

    void check() {
        if (client.getState() == WsClient.STATE_CLOSED) {
            return;
        }
        SLogger.d(TAG, "reconnecting the server");
        client.connect(wsUrl)
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
