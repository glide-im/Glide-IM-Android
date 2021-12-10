package pro.glideim.sdk.ws;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.glideim.sdk.protocol.CommMessage;
import pro.glideim.sdk.api.auth.LoginDto;
import pro.glideim.sdk.http.RetrofitManager;

import java.util.concurrent.ExecutionException;

class WsClientTest {

    @BeforeEach
    void setUp() {
        RetrofitManager.init("http://localhost:8081/api/");
    }

    @Test
    void connect() throws InterruptedException {
        WsClient c = new WsClient();
        try {
            c.connect("ws://localhost:8080/ws");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        c.sendMessage(new CommMessage(1, "api.user.login", 1, new LoginDto("abc", "abc", 1)));
        Thread.sleep(5000);
    }
}