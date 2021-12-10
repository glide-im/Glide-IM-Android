package pro.glideim.sdk;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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
    }

    @Test
    void getUserInfo() {
        GlideIM.getUserInfo(Arrays.asList(1L, 2L, 3L))
        .subscribe(new TestObserver<>());
    }
}