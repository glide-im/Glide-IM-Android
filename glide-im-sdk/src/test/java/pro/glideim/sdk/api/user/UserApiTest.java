package pro.glideim.sdk.api.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.glideim.sdk.TestObserver;
import pro.glideim.sdk.http.RetrofitManager;

import java.util.Arrays;

class UserApiTest {

    @BeforeEach
    void tearDown() {
        RetrofitManager.init("http://localhost:8081/api/");
    }

    @Test
    void getContactsList() {
        UserApi.API.getContactsList().subscribe(new TestObserver<>());
    }

    @Test
    void addContacts() {
        UserApi.API.addContacts(new ContactsUidDto(543614, "")).subscribe(new TestObserver<>());
    }

    @Test
    void delContacts() {

    }

    @Test
    void contactsApproval() {
    }

    @Test
    void getUserInfo() {
        UserApi.API.getUserInfo(new GetUserInfoDto(Arrays.asList(1L, 2L, 543614L))).subscribe(new TestObserver<>());
    }

    @Test
    void myProfile() {

    }

    @Test
    void updateMyProfile() {
    }
}