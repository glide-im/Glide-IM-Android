package pro.glideim.sdk.im;


import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;
import pro.glideim.sdk.ws.WsClient;

public class KeepAlive implements ConnStateListener {

    private static final String TAG = KeepAlive.class.getSimpleName();

    private final WsClient client;
    private boolean reconnecting = false;
    private boolean stopped = false;
    private Disposable reconnect;
    private int retry = 0;

    private KeepAlive(WsClient client) {
        this.client = client;
        this.client.addStateListener(this);
    }

    static KeepAlive create(WsClient client) {
        return new KeepAlive(client);
    }

    @Override
    public void onStateChange(int state, String msg) {
        if (state == WsClient.STATE_CLOSED) {
            check();
        }
    }

    void start() {
        stop();
        stopped = false;
    }

    void stop() {
        stopped = true;
        if (reconnect != null && !reconnect.isDisposed()) {
            reconnect.dispose();
        }
    }

    void check() {
        if (stopped) {
            return;
        }
        if (client.getState() != WsClient.STATE_CLOSED || reconnecting) {
            return;
        }
        if (reconnect != null && !reconnect.isDisposed()) {
            reconnect.dispose();
        }
        reconnecting = true;
        retry = 0;
        reconnect = client
                .connect()
                .compose(RxUtils.silentSchedulerSingle())
                .retryWhen(throwableFlowable -> throwableFlowable.<Object>flatMap(throwable -> {
                    if (stopped || reconnect.isDisposed()) {
                        return Flowable.error(throwable);
                    }
                    retry++;
                    SLogger.d(TAG, "reconnect server failed: " + throwable.getMessage() + ", retry times:" + retry);
                    return Flowable.timer(3, TimeUnit.SECONDS);
                }).onErrorResumeNext((Function<Throwable, Publisher<?>>) Flowable::error))
                .doOnSubscribe(disposable -> {
                    SLogger.d(TAG, "reconnecting the server");
                })
                .doOnSuccess(aBoolean -> {
                    reconnecting = false;
                    SLogger.d(TAG, "reconnect server success");
                })
                .doOnError(e -> {
                    SLogger.d(TAG, "reconnect server failed: " + e.getMessage());
                })
                .subscribe();
    }
}
