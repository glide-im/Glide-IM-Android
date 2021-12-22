package pro.glideim.sdk.ws;

import org.junit.jupiter.api.Test;

import io.reactivex.Single;

class NettyTest {

    @Test
    void connect2() throws InterruptedException {
        Single<Boolean> connect = new NettyWsClient().connect("ws://127.0.0.1:8080/ws");
        connect.blockingGet();
        Thread.sleep(3000);
    }
}