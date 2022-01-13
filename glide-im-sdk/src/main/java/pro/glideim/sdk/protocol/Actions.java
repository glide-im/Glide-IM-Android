package pro.glideim.sdk.protocol;

public interface Actions {

    // server down
    String ACTION_ACK_MESSAGE = "ack.message";
    String ACTION_ACK_NOTIFY = "ack.notify";
    // client up
    String ACTION_AKC_REQUEST = "ack.request";
    String ACTION_ACK_GROUP_MSG = "ack.group.msg";

    // message
    String ACTION_MESSAGE_CHAT = "message.chat";
    String ACTION_MESSAGE_GROUP = "message.group";
    String ACTION_MESSAGE_CHAT_RETRY = "message.chat.retry";
    String ACTION_MESSAGE_CHAT_RESEND = "message.chat.resend";
    String ACTION_MESSAGE_FAILED_SEND = "message.failed.send";

    // control
    String ACTION_HEARTBEAT = "heartbeat";
    String ACTION_NOTIFY = "notify";

    interface Srv {
        String ACTION_API_SUCCESS = "api.success";
        String ACTION_NOTIFY_NEED_AUTH = "notify.auth";
        String ACTION_NEW_CONTACT = "notify.contact";
        String ACTION_KICK_OUT = "notify.kickout";
    }

    interface Cli {
        String ACTION_ACK_REQUEST = "ack.request";

        String ACTION_API_USER_AUTH = "api.user.auth";
        String ACTION_API_LOGOUT = "api.user.logout";
    }
}
