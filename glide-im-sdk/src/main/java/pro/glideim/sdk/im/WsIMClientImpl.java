package pro.glideim.sdk.im;

import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.internal.operators.observable.ObservableSubscribeOn;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import pro.glideim.sdk.Logger;
import pro.glideim.sdk.ParameterizedTypeImpl;
import pro.glideim.sdk.http.RetrofitManager;
import pro.glideim.sdk.protocol.AckMessage;
import pro.glideim.sdk.protocol.AckRequest;
import pro.glideim.sdk.protocol.Actions;
import pro.glideim.sdk.protocol.ChatMessage;
import pro.glideim.sdk.protocol.CommMessage;

public class WsIMClientImpl implements IMClient {

    public static final int STATE_CLOSED = 1;
    public static final int STATE_OPENED = 2;
    public static final int STATE_CONNECTING = 3;

    private static final int MESSAGE_VER = 1;
    private static final int TIMEOUT_REQUEST_SEC = 5;
    private static final int TIMEOUT_MESSAGE = 5;
    private static final int SEND_MESSAGE_RETRY_TIMES = 5;

    private final Logger logger;
    private final WsClient wsClient;
    private final Map<Long, RequestEmitter> requests = new ConcurrentHashMap<>();
    private final Map<Long, MessageEmitter> messages = new ConcurrentHashMap<>();
    private final Type typeCommMsg = new TypeToken<CommMessage<Object>>() {
    }.getType();
    private MessageListener messageListener;
    private boolean open;
    private long seq;

    private ConnStateListener connStateListener;

    private WsIMClientImpl() {
        wsClient = new WsClient();
        logger = new Logger() {
            @Override
            public void d(String tag, String log) {
                System.out.println(tag + "\t" + log);
            }

            @Override
            public void e(String tag, Throwable t) {
                System.err.println(tag + "\t" + t.getMessage());
                t.printStackTrace();
            }
        };
    }

    public static WsIMClientImpl create() {
        return new WsIMClientImpl();
    }

    private static <T> T deserialize(Type t, Message msg) {
        return RetrofitManager.fromJson(t, msg.message);
    }

    public void setConnStateListener(ConnStateListener connStateListener) {
        this.connStateListener = connStateListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void connect(String url, IMConnectListener l) {
        wsClient.setListener(new WsListener(l));
        try {
            wsClient.connect(url);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        wsClient.disconnect();
    }

    public Observable<ChatMessage> resendMessage(ChatMessage message) {
        return sendMessage(Actions.ACTION_MESSAGE_CHAT_RESEND, message);
    }

    public Observable<ChatMessage> sendChatMessage(ChatMessage message) {
        return sendMessage(Actions.ACTION_MESSAGE_CHAT, message);
    }

    public Observable<ChatMessage> sendGroupMessage(ChatMessage message) {
        return sendMessage(Actions.ACTION_MESSAGE_GROUP, message);
    }

    public <T> Observable<CommMessage<T>> request(String action, Class<T> clazz, boolean isArray, Object data) {
        if (!open) {
            return Observable.error(new Exception("the server is not connected"));
        }
        CommMessage<Object> m = new CommMessage<>(MESSAGE_VER, action, seq++, data);

        Type t;
        if (isArray) {
            t = new ParameterizedTypeImpl(List.class, new Class[]{clazz});
            t = new ParameterizedTypeImpl(CommMessage.class, new Type[]{t});
        } else {
            t = new ParameterizedTypeImpl(CommMessage.class, new Class[]{clazz});
        }
        final Type finalT = t;
        return ObservableSubscribeOn.<CommMessage<T>>create(emitter -> {
            boolean success = send(m);
            if (!success) {
                emitter.onError(new Exception("message send failed"));
            } else {
                requests.put(m.getSeq(), new RequestEmitter(emitter, finalT));
            }
        }).timeout(TIMEOUT_REQUEST_SEC, TimeUnit.SECONDS);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private Observable<ChatMessage> sendMessage(final String action, final ChatMessage message) {
        if (!open) {
            return Observable.error(new Exception("the server is not connected"));
        }

        Observable<ChatMessage> ob = ObservableSubscribeOn.create(emitter -> {
            MessageEmitter e = new MessageEmitter(emitter, message);
            messages.put(message.getMid(), e);
            e.send(action);
        });
        Observable<ChatMessage> flat = ob
                .timeout(TIMEOUT_MESSAGE, TimeUnit.SECONDS)
                .doOnNext(msg -> {
                    if (msg.getState() == ChatMessage.STATE_RCV_RECEIVED) {
                        messages.remove(msg.getMid());
                    }
                });
        return flat;
    }

    private boolean send(Object obj) {
        return wsClient.sendMessage(obj);
    }

    private void onMessage(Message msg) {
        CommMessage<Object> m = deserialize(typeCommMsg, msg);
        if (m.getAction().startsWith("api")) {
            if (requests.containsKey(m.getSeq())) {
                requests.get(m.getSeq()).respond(m, msg);
                requests.remove(m.getSeq());
            } else {
                logger.d("onMessage", "unknown api response");
            }
            return;
        }
        logger.d("IMClient.onMessage: ", m.toString());
        switch (m.getAction()) {
            case Actions.ACTION_MESSAGE_CHAT:
                onChatMessage(msg);
                return;
            case Actions.ACTION_ACK_MESSAGE:
            case Actions.ACTION_ACK_NOTIFY:
                onAck(msg);
                return;
            case Actions.ACTION_ACK_GROUP_MSG:
                onGroupMessage(msg);
                return;
            case Actions.ACTION_NOTIFY:
                return;
        }

        logger.d("IMClient.onMessage:", "UNKNOWN ACTION: " + m.getAction());
    }

    private void onAck(Message msg) {
        Type type = new TypeToken<CommMessage<AckMessage>>() {
        }.getType();
        CommMessage<AckMessage> m = deserialize(type, msg);
        AckMessage ack = m.getData();
        if (ack == null) {
            throw new NullPointerException("ack message data is null");
        }
        if (!messages.containsKey(ack.getMid())) {
            logger.d("IMClient.onAckMessage", "ack mid not exist");
            return;
        }
        MessageEmitter emitter = messages.get(m.getData().getMid());
        if (emitter != null) {
            emitter.onAck(m);
        } else {
            logger.d("IMClient.onAckMessage", "ack message emitter null");
        }
    }

    private void onGroupMessage(Message msg) {

    }

    private void onChatMessage(Message msg) {
        Type type = new TypeToken<CommMessage<ChatMessage>>() {
        }.getType();
        CommMessage<ChatMessage> c = deserialize(type, msg);
        ChatMessage cm = c.getData();
        if (messageListener != null) {
            messageListener.onNewMessage(cm);
        }
        // ACK
        AckRequest a = new AckRequest(cm.getMid(), cm.getFrom(), 0);
        logger.d("IMClient.onChatMessage:", "send ack");
        send(new CommMessage<>(MESSAGE_VER, Actions.Cli.ACTION_ACK_REQUEST, 0, a));
    }

    private void onConnStateChanged(int state, String msg) {
        if (connStateListener != null) {
            connStateListener.onStateChange(state, msg);
        }
    }

    public interface MessageListener {
        void onNewMessage(ChatMessage m);
    }

    @SuppressWarnings("rawtypes")
    private static class RequestEmitter {
        private final ObservableEmitter emitter;
        private final Type type;

        public RequestEmitter(ObservableEmitter emitter, Type t) {
            this.emitter = emitter;
            this.type = t;
        }

        void respond(CommMessage<Object> m, Message msg) {
            if (m.getAction().equals("api.success")) {
                try {
                    CommMessage<Object> o = deserialize(type, msg);
                    //noinspection unchecked
                    emitter.onNext(o);
                } catch (Throwable e) {
                    this.emitter.onError(new Exception("json parse error, " + e.getMessage()));
                }
            } else {
                emitter.onError(new Exception(m.getData().toString()));
            }
            emitter.onComplete();
        }
    }

    private class MessageEmitter {
        private final ObservableEmitter<ChatMessage> emitter;
        int retry;
        ChatMessage msg;

        public MessageEmitter(ObservableEmitter<ChatMessage> emitter, ChatMessage msg) {
            this.emitter = emitter;
            this.msg = msg;
        }

        void send(String action) {
            CommMessage<ChatMessage> c = new CommMessage<>(MESSAGE_VER, action, 0, msg);
            boolean send = WsIMClientImpl.this.send(c);
            if (!send) {
                if (WsIMClientImpl.this.open) {
                    emitter.onError(new Exception("send message failed"));
                } else {
                    emitter.onError(new Exception("send message failed due to socket closed"));
                }
            } else {
                emitter.onNext(msg.setState(ChatMessage.STATE_SRV_SENDING));
            }
        }

        void onAck(CommMessage<AckMessage> a) {
            switch (a.getAction()) {
                case Actions.ACTION_ACK_MESSAGE:
                    emitter.onNext(msg.setState(ChatMessage.STATE_SRV_RECEIVED));
                    break;
                case Actions.ACTION_ACK_GROUP_MSG:
                case Actions.ACTION_ACK_NOTIFY:
                    emitter.onNext(msg.setState(ChatMessage.STATE_RCV_RECEIVED));
                    emitter.onComplete();
                    break;
            }
        }
    }

    private final class WsListener extends WebSocketListener {

        private IMConnectListener wsConnectListener;

        WsListener(IMConnectListener l) {
            this.wsConnectListener = l;
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            WsIMClientImpl.this.disconnect();
            WsIMClientImpl.this.onConnStateChanged(STATE_CLOSED, reason);
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            try {
                WsIMClientImpl.this.onMessage(new Message(text));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            open = false;
        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            if (wsConnectListener != null) {
                wsConnectListener.onSuccess();
                wsConnectListener = null;
            }
            open = true;
            WsIMClientImpl.this.onConnStateChanged(STATE_OPENED, "");
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            open = false;
            WsIMClientImpl.this.onConnStateChanged(STATE_CLOSED, t.getMessage());
            if (wsConnectListener != null) {
                wsConnectListener.onError(t);
                wsConnectListener = null;
            }
        }
    }
}
