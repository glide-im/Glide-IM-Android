package pro.glideim.sdk.api.msg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.glideim.sdk.TestObserver;
import pro.glideim.sdk.http.RetrofitManager;

import java.util.Arrays;

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
        MsgApi.API.getChatMessageHistory(new GetChatHistoryDto(2)).subscribe(new TestObserver<>());
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