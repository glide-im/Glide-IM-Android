package pro.glideim.sdk;

import androidx.annotation.NonNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import pro.glideim.sdk.api.user.UserInfoBean;

class GlideIMTest {

    @BeforeEach
    void setUp() {
        GlideIM.init("ws://192.168.1.123:8080/ws", "http://192.168.1.123:8081/api/");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void init() {
    }

    @Test
    void getContacts() {
        GlideIM.getContacts();
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