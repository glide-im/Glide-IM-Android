package pro.glideim.sdk.ws;

import org.junit.jupiter.api.Test;

import io.reactivex.Single;

class NettyTest {

    @Test
    void connect2() throws InterruptedException {
        Single<Boolean> connect = new NettyWsClient("ws://localhost:8080/ws").connect();
        connect.blockingGet();
        Thread.sleep(3000);
    }
}