package pro.glideim.sdk.entity;

import android.opengl.EGLImage;

import androidx.annotation.NonNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import pro.glideim.sdk.GlideIM;
import pro.glideim.sdk.TestObserver;
import pro.glideim.sdk.TestResObserver;

class IMSessionListTest {

    @BeforeEach
    void setUp() throws InterruptedException {
        GlideIM.init("ws://192.168.1.123:8080/ws", "http://192.168.1.123:8081/api/");
        GlideIM.getInstance().connect().blockingGet();
        GlideIM.login("abc", "abc", 1)
                .subscribe(new TestObserver<>());
        Thread.sleep(1000);
    }

    @AfterEach
    void tearDown() {

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
        IMSessionList imSessionList = GlideIM.getInstance().getAccount().getIMSessionList();
        imSessionList.setSessionUpdateListener(new SessionUpdateListener() {
            @Override
            public void onUpdate(IMSession... session) {
                System.out.println("SessionUpdateListener.onUpdate:------------------");
                for (IMSession s : session) {
                    System.out.println(">>>>>"+s.toString());
                }
            }

            @Override
            public void onNewSession(IMSession... session) {
                System.out.println("SessionUpdateListener.onNewSession:++++++++++++++++++");
                for (IMSession s : session) {
                    System.out.println("!!!!!!"+s.toString());
                }
            }
        });
        imSessionList.initSessionsList().subscribe(new TestResObserver<Boolean>() {
            @Override
            public void onNext(@NonNull Boolean aBoolean) {
                System.out.println("initSessionList.onNext =====================");
            }
        });
        Thread.sleep(3000);
    }
}