package pro.glideim.sdk.api.msg;

import androidx.annotation.NonNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pro.glideim.sdk.IMMessage;
import pro.glideim.sdk.TestObserver;
import pro.glideim.sdk.http.RetrofitManager;
import pro.glideim.sdk.utils.RxUtils;

import java.util.Arrays;
import java.util.List;

class MsgApiTest {

    @BeforeEach
    void setUp() {
        RetrofitManager.init("http://localhost:8081/api/");
    }

    @Test
    void recentMsg() {
        MsgApi.API.getChatMessageByUsers(new GetUserMsgDto(Arrays.asList(1, 2, 3, 4, 5, 6))).subscribe(new TestObserver<>());
    }

    @Test
    void history() {
        MsgApi.API.getChatMessageHistory(new GetChatHistoryDto(2,0))
                .map(RxUtils.bodyConverter())
                .subscribe(new TestObserver<List<MessageBean>>(){
                    @Override
                    public void onNext(@NonNull List<MessageBean> messageBeans) {
                        System.out.println("history.onNext:"+messageBeans.toString());
                    }
                });
    }

    @Test
    void getOfflineMsg() {

    }

    @Test
    void ackOfflineMsg() {
    }

    @Test
    void getRecentSession() {
        MsgApi.API.getRecentSession().subscribe(new TestObserver<>());
    }

    @Test
    void getSession() {
        MsgApi.API.getSession(new GetSessionDto(2)).subscribe(new TestObserver<>());
    }

    @Test
    void updateSession() {
        MsgApi.API.updateSession(new GetSessionDto(2)).subscribe(new TestObserver<>());
    }
}