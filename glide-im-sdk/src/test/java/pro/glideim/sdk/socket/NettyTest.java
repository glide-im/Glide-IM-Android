package pro.glideim.sdk.socket;

import org.junit.jupiter.api.Test;

class NettyTest {

    @Test
    void init() {
        Netty.connect("127.0.0.1", 8080);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}