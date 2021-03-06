package pro.glideim.sdk;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import pro.glideim.sdk.api.auth.AuthApi;
import pro.glideim.sdk.api.auth.AuthBean;
import pro.glideim.sdk.api.auth.LoginDto;
import pro.glideim.sdk.api.auth.RegisterDto;
import pro.glideim.sdk.http.RetrofitManager;
import pro.glideim.sdk.im.IMClientImpl;
import pro.glideim.sdk.messages.ChatMessage;
import pro.glideim.sdk.messages.CommMessage;

public class MockUserTest {

    IMClientImpl imClient = IMClientImpl.create("ws://localhost:8080/ws");

    @BeforeEach
    void setup() throws InterruptedException {

        RetrofitManager.init("http://localhost:8081/api/");
        imClient.connect().blockingGet();
//        imClient.setMessageListener(m -> {
//            System.out.println("On Receive Message ===>>> " + RetrofitManager.toJson(m));
//        });

        Thread.sleep(1000);
    }

    @AfterEach
    void setdown() {
        imClient.disconnect();
    }

    @Test
    void register() {
        AuthApi.API.register(new RegisterDto("aaa", "aaa")).subscribe(new TestObserver<>());
    }

    @Test
    void login() throws InterruptedException {
        LoginDto d = new LoginDto("aaa", "aaa", 1);
        Observable<CommMessage<AuthBean>> request = imClient.request("api.user.login", AuthBean.class, false, d);
        request.subscribe(new TestObserver<>());

        Thread.sleep(1000);
        ChatMessage c = new ChatMessage();
        c.setTo(5436191);
        c.setcSeq(1);
        c.setContent("hello");
        c.setType(1);
        c.setMid(123431);
        c.setcTime(System.currentTimeMillis());
        Observable<ChatMessage> o = imClient.sendChatMessage(c);
        o.subscribe(new Observer<ChatMessage>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {

            }

            @Override
            public void onNext(@NotNull ChatMessage message) {
                System.out.println("send.onNext:" + message.getState());
            }

            @Override
            public void onError(@NotNull Throwable e) {
                System.out.println("send.onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });

        Thread.sleep(60000);
    }
}
