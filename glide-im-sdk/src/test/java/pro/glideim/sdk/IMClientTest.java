package pro.glideim.sdk;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.glideim.sdk.api.auth.LoginDto;
import pro.glideim.sdk.api.auth.AuthBean;
import pro.glideim.sdk.http.RetrofitManager;
import pro.glideim.sdk.protocol.ChatMessage;
import pro.glideim.sdk.protocol.CommMessage;
import pro.glideim.sdk.im.IMClientImpl;

class IMClientTest {

    IMClientImpl imClient = IMClientImpl.create();

    @BeforeEach
    void setUp() throws InterruptedException {
        RetrofitManager.init("http://localhost/api/");
        Boolean aBoolean = imClient.connect("ws://localhost:8080/ws").blockingGet();
        Thread.sleep(1000);
    }

    @AfterEach
    void down() {
        imClient.disconnect();
    }

    @Test
    void connect() throws InterruptedException {
        LoginDto d = new LoginDto("abc", "abc", 1);
        Observable<CommMessage<AuthBean>> ob = imClient.request("api.user.login", AuthBean.class, false, d);
        ob.observeOn(Schedulers.single())
                .subscribe(new Observer<CommMessage<AuthBean>>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {
                        System.out.println("IMClientTest.onSubscribe");
                    }

                    @Override
                    public void onNext(@NotNull CommMessage<AuthBean> tokenBean) {
                        System.out.println("IMClientTest.onNext\t" + tokenBean);
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        System.out.println("IMClientTest.onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("IMClientTest.onComplete");
                    }
                });
        Thread.sleep(5000);
    }

    @Test
    void sendChatMessage() throws InterruptedException {
        ChatMessage c = new ChatMessage();
        c.setTo(543619);
        c.setcSeq(1);
        c.setContent("hello");
        c.setType(1);
        c.setMid(12343);
        c.setcTime(System.currentTimeMillis());
        Observable<ChatMessage> o = imClient.sendChatMessage(c);
        o.subscribe(new TestObserver<>());
        Thread.sleep(4000);
    }

    @Test
    void send() {

    }
}