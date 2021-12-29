package pro.glideim.sdk.im;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import pro.glideim.sdk.protocol.Actions;
import pro.glideim.sdk.protocol.CommMessage;
import pro.glideim.sdk.ws.WsClient;

public class Heartbeat implements ConnStateListener {

    private final IMClient client;
    private Disposable heartbeat;

    private Heartbeat(IMClient client) {
        this.client = client;
    }

    public static Heartbeat start(IMClient client) {
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

    private void start() {
        stop();
        heartbeat = Observable
                .interval(3, TimeUnit.SECONDS)
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
        return heartbeat.isDisposed();
    }

    public void stop() {
        if (heartbeat != null && !heartbeat.isDisposed()) {
            heartbeat.dispose();
        }
    }
}
