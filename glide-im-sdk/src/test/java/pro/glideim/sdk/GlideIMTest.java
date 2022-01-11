package pro.glideim.sdk;

import androidx.annotation.NonNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import pro.glideim.sdk.api.group.CreateGroupBean;
import pro.glideim.sdk.api.user.UserInfoBean;

class GlideIMTest {

    @BeforeEach
    void setUp() throws InterruptedException {
        GlideIM.init("http://192.168.1.123:8081/api/");
        GlideIM.getAccount().getIMClient().connect().blockingGet();
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