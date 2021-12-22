package pro.glideim.sdk;

import androidx.annotation.NonNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import pro.glideim.sdk.api.group.CreateGroupBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.entity.IMContacts;
import pro.glideim.sdk.entity.IMMessage;
import pro.glideim.sdk.entity.IMSession;
import pro.glideim.sdk.protocol.ChatMessage;

class GlideIMTest {

    @BeforeEach
    void setUp() throws InterruptedException {
        GlideIM.init("ws://192.168.1.123:8080/ws", "http://192.168.1.123:8081/api/");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void login() {
        GlideIM.login("abc", "abc", 1)
                .subscribe(new TestObserver<>());
    }

    @Test
    void sendChatMessage() throws InterruptedException {
        login();
        Thread.sleep(1000);
        GlideIM.sendChatMessage(543604L, 1, "hello world")
                .subscribe(new TestResObserver<IMMessage>() {
                    @Override
                    public void onNext(@NonNull IMMessage chatMessage) {
                        System.out.println("===================" + chatMessage.getState());
                    }
                });
        Thread.sleep(50000);
    }

    @Test
    void getContacts() {
        GlideIM.getContacts().subscribe(new TestResObserver<List<IMContacts>>() {
            @Override
            public void onNext(@NonNull List<IMContacts> contacts) {
                for (IMContacts contact : contacts) {
                    System.out.println("getContacts.onNext: " + contact);
                }
            }
        });
    }

    @Test
    void createGroup() {
        GlideIM.createGroup("HelloGroup")
                .subscribe(new TestResObserver<CreateGroupBean>() {
                    @Override
                    public void onNext(@NonNull CreateGroupBean createGroupBean) {
                        System.out.println("createGroup.onNext: " + createGroupBean.getGid());
                    }
                });
    }

    @Test
    void getRecentMessages() throws InterruptedException {
        GlideIM.getContacts().subscribe(new TestResObserver<List<IMContacts>>() {
            @Override
            public void onNext(@NonNull List<IMContacts> contacts) {
                GlideIM.getSessionList().subscribe(new TestResObserver<List<IMSession>>() {
                    @Override
                    public void onNext(@NonNull List<IMSession> sessions) {
                        GlideIM.updateSessionList()
                                .subscribe(new TestResObserver<List<IMSession>>() {
                                    @Override
                                    public void onNext(@NonNull List<IMSession> sessions) {

                                    }
                                });
                    }
                });
            }
        });
    }

    @Test
    void getMessageHistory() {
        GlideIM.getChatMessageHistory(2, 1099)
                .subscribe(new TestResObserver<List<IMMessage>>() {
                    @Override
                    public void onNext(@NonNull List<IMMessage> imMessages) {
                        for (IMMessage imMessage : imMessages) {
                            System.out.println(imMessage.toString());
                        }
                    }
                });
    }

    @Test
    public void getUserInfo() {
        GlideIM.getUserInfo(Arrays.asList(543602L, 543603L))
                .subscribe(new TestResObserver<List<UserInfoBean>>() {
                    @Override
                    public void onNext(@NonNull List<UserInfoBean> userInfoBeans) {

                        for (UserInfoBean datum : userInfoBeans) {
                            System.out.println(datum.getNickname());
                        }
                    }
                });
    }
}