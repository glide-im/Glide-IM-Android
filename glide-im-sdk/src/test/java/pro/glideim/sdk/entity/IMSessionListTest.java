package pro.glideim.sdk.entity;

import androidx.annotation.NonNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import pro.glideim.sdk.GlideIM;
import pro.glideim.sdk.IMMessage;
import pro.glideim.sdk.IMSession;
import pro.glideim.sdk.IMSessionList;
import pro.glideim.sdk.SessionUpdateListener;
import pro.glideim.sdk.TestResObserver;

class IMSessionListTest {

    @BeforeEach
    void setUp() {
        GlideIM.init("http://192.168.1.123:8081/api/");
        GlideIM.login("abc", "abc", 1).blockingFirst();
    }

    @AfterEach
    void tearDown() {
        GlideIM.getAccount().logout();
    }

    @Test
    void sendMessage() {
        GlideIM.getAccount().getIMSessionList().loadSessionsList().blockingFirst();
        IMSession imSession = GlideIM.getAccount().getIMSessionList().getSessions().get(0);
        imSession.sendTextMessage("~~~")
                .subscribe(new TestResObserver<IMMessage>() {
                    @Override
                    public void onNext(@NonNull IMMessage imMessage) {
                        System.out.println("sendMessage.onNext: id=" + imMessage.getMid() + ", state=" + imMessage.getState());
                    }
                });

    }

    @Test
    void setSessionUpdateListener() {
    }

    @Test
    void getSession() {
    }

    @Test
    void containSession() {
    }

    @Test
    void addMessage() {
    }

    @Test
    void setSessionRecentMessages() {
    }

    @Test
    void testGetSession() {
    }

    @Test
    void getSessionList() {
    }

    @Test
    void updateSessionList() throws InterruptedException {
        IMSessionList imSessionList = GlideIM.getAccount().getIMSessionList();
        imSessionList.setSessionUpdateListener(new SessionUpdateListener() {
            @Override
            public void onUpdate(IMSession session) {
                System.out.println("SessionUpdateListener.onUpdate: " + session.to);
            }

            @Override
            public void onNewSession(IMSession session) {
                System.out.println("SessionUpdateListener.onNewSession: " + session.to);

            }
        });
        imSessionList.loadSessionsList()
                .subscribe(new TestResObserver<IMSession>() {
                    @Override
                    public void onNext(@NonNull IMSession session) {

                    }
                });
        Thread.sleep(3000);
    }
}