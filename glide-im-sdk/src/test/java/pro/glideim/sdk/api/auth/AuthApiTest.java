package pro.glideim.sdk.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.glideim.sdk.TestObserver;
import pro.glideim.sdk.http.RetrofitManager;

class AuthApiTest {

    @BeforeEach
    void tearDown() {
        RetrofitManager.init("http://localhost:8081/api/");
    }

    @Test
    void register() {
        RegisterDto d = new RegisterDto("love2", "password");
        UserApi.API.register(d).subscribe(new TestObserver<>());
    }

    @Test
    void login() {
        LoginDto d = new LoginDto("love2","password", 1);
        UserApi.API.login(d).subscribe(new TestObserver<>());
    }

    @Test
    void logout() {
        UserApi.API.logout().subscribe(new TestObserver<>());
    }
}