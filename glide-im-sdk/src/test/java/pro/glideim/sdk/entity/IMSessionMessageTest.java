package pro.glideim.sdk.entity;

import org.junit.jupiter.api.Test;

import pro.glideim.sdk.api.msg.GroupMessageBean;
import pro.glideim.sdk.protocol.ChatMessage;

class IMSessionMessageTest {

    @Test
    void addMessage() {
    }

    @Test
    void addMessages() {
    }

    @Test
    void getLatest() {
        IMSessionMessage m = IMSession.create(1,1).getMessages();
        for (int i = 0; i < 29; i++) {
            GroupMessageBean cm = new GroupMessageBean();
            cm.setMid(i);
            m.addMessage(IMMessage.fromGroupMessage(cm));
        }
        for (IMMessage imMessage : m.getLatest()) {
            System.out.println(imMessage.getMid());
        }
    }
}