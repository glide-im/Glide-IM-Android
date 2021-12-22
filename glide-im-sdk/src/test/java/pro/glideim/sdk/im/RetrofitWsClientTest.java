package pro.glideim.sdk.im;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pro.glideim.sdk.http.RetrofitManager;
import pro.glideim.sdk.ws.RetrofitWsClient;

import java.util.concurrent.ExecutionException;

class RetrofitWsClientTest {

    @BeforeEach
    void setUp() {
        RetrofitManager.init("http://localhost:8081/api/");
    }

    @Test
    void connect() throws InterruptedException {
        RetrofitWsClient c = new RetrofitWsClient();
        c.connect("ws://localhost:8080/ws").blockingGet();

//        c.sendMessage(new CommMessage(1, "api.user.login", 1, new LoginDto("abc", "abc", 1)));
        Thread.sleep(10000);
    }
}