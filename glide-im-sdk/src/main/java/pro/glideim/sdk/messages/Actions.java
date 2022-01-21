package pro.glideim.sdk.messages;

public interface Actions {

    // control
    String ACTION_HEARTBEAT = "heartbeat";

    interface Srv {
        String ACTION_ACK_MESSAGE = "ack.message";
        String ACTION_ACK_NOTIFY = "ack.notify";
        String ACTION_API_SUCCESS = "api.success";

        String ACTION_NOTIFY_NEED_AUTH = "notify.auth";
        String ACTION_NEW_CONTACT = "notify.contact";
        String ACTION_KICK_OUT = "notify.kickout";
        String ACTION_NOTIFY_ERROR = "notify.error";
        String ACTION_NOTIFY_GROUP = "notify.group";
        String ACTION_NOTIFY_LOGIN = "notify.login";
        String ACTION_NOTIFY_LOGOUT = "notify.logout";

        String ACTION_MESSAGE_CHAT = "message.chat";
        String ACTION_MESSAGE_GROUP = "message.group";
    }

    interface Cli {
        String ACTION_ACK_REQUEST = "ack.request";
        String ACTION_API_USER_AUTH = "api.user.auth";
        String ACTION_API_LOGOUT = "api.user.logout";
        String ACTION_ACK_GROUP_MSG = "ack.group.msg";
        String ACTION_MESSAGE_CHAT_RETRY = "message.chat.retry";
        String ACTION_MESSAGE_CHAT_RESEND = "message.chat.resend";
    }
}
