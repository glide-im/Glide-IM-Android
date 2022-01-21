package pro.glideim.sdk.im;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import pro.glideim.sdk.messages.Actions;
import pro.glideim.sdk.messages.CommMessage;
import pro.glideim.sdk.utils.SLogger;
import pro.glideim.sdk.ws.WsClient;

public class Heartbeat implements ConnStateListener {

    private static final String TAG = Heartbeat.class.getSimpleName();
    private final pro.glideim.sdk.im.IMClient client;
    private Disposable disposable;

    private Heartbeat(pro.glideim.sdk.im.IMClient client) {
        this.client = client;
        this.client.getWebSocketClient().addStateListener(this);
    }

    public static Heartbeat start(pro.glideim.sdk.im.IMClient client) {
        Heartbeat heartbeat = new Heartbeat(client);
        heartbeat.start();
        return heartbeat;
    }

    @Override
    public void onStateChange(int state, String msg) {
        if (state == WsClient.STATE_OPENED) {
            start();
        } else if (state == WsClient.STATE_CLOSED) {
            stop();
        }
    }

    public void start() {
        stop();
        SLogger.d(TAG, "start");
        Observable.interval(3, TimeUnit.SECONDS)
                .doOnSubscribe(disposable -> {
                    stop();
                    this.disposable = disposable;
                })
                .doOnNext(aLong -> {
                    if (client.isConnected()) {
                        client.send(new CommMessage<>(1, Actions.ACTION_HEARTBEAT, 0, ""));
                    } else {
                        stop();
                    }
                })
                .subscribe();
    }

    public boolean isRunning() {
        return disposable.isDisposed();
    }

    public void stop() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
