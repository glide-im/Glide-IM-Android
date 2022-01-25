package pro.glideim.sdk;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.msg.GetChatHistoryDto;
import pro.glideim.sdk.api.msg.GetGroupMsgHistoryDto;
import pro.glideim.sdk.api.msg.GetSessionDto;
import pro.glideim.sdk.api.msg.GroupMessageBean;
import pro.glideim.sdk.api.msg.GroupMessageStateBean;
import pro.glideim.sdk.api.msg.MessageBean;
import pro.glideim.sdk.api.msg.MessageIDBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.api.msg.SessionBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.im.IMClient;
import pro.glideim.sdk.messages.ChatMessage;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;

public class IMSession {

    public static final String TAG = IMSession.class.getSimpleName();
    private final TreeMap<Long, IMMessage> messageTreeMap = new TreeMap<>();

    private final IMAccount account;
    public long to;
    public long lastMsgSender;
    public String title;
    public String avatar;
    public int unread;
    public long updateAt;
    public long previousUpdateAt;
    public int type;
    public String lastMsg;
    public long lastMsgId;
    public long createAt;

    IMSessionList.SessionTag tag;
    private SessionUpdateListener sessionUpdateListener;
    private MessageChangeListener messageChangeListener;

    private boolean infoInit = false;
    private long lastReadMid = 0;

    public IMSession(IMAccount account, long to, int type) {
        this.tag = IMSessionList.SessionTag.get(type, to);
        this.to = to;
        this.type = type;
        this.title = String.valueOf(to);
        this.avatar = "";
        setUpdateAt(System.currentTimeMillis() / 1000);
        this.createAt = updateAt;
        this.account = account;
    }

    static IMSession create(IMAccount account, GroupMessageStateBean stateBean) {
        IMSession s = new IMSession(account, stateBean.getGid(), Constants.SESSION_TYPE_GROUP);
        s.unread = 0;
        s.lastMsgId = stateBean.getLastMID();
        return s;
    }

    static IMSession create(IMAccount account, SessionBean sessionBean) {
        IMSession s;
        if (sessionBean.getUid1() == account.uid) {
            s = new IMSession(account, sessionBean.getUid2(), Constants.SESSION_TYPE_USER);
        } else {
            s = new IMSession(account, sessionBean.getUid1(), Constants.SESSION_TYPE_USER);
        }
        s.setUpdateAt(sessionBean.getUpdateAt());
        s.lastMsgId = sessionBean.getLastMid();
        s.createAt = sessionBean.getCreateAt();
        return s;
    }

    static IMSession create(IMAccount account, long to, int type) {
        IMSession s = new IMSession(account, to, type);
        s.initInfo()
                .compose(RxUtils.silentScheduler())
                .subscribe(new SilentObserver<>());
        return s;
    }

    public IMSession merge(IMSession session) {
        this.setUpdateAt(session.updateAt);
        this.lastMsg = session.lastMsg;
        this.messageTreeMap.putAll(session.messageTreeMap);
        return this;
    }

    public IMMessage getMessage(long mid) {
        return messageTreeMap.get(mid);
    }

    private void onSendMessageCreated(IMMessage msg) {
        SLogger.d(TAG, "onSendMessageCreated:" + msg);
        GlideIM.getDataStorage().storeMessage(msg);
        messageTreeMap.put(msg.getMid(), msg);
        setLastMessage(msg);
        onSessionUpdate();
    }

    public void setSessionUpdateListener(SessionUpdateListener sessionUpdateListener) {
        this.sessionUpdateListener = sessionUpdateListener;
    }

    public void syncStatus() {
        MsgApi.API.getSession(new GetSessionDto(to))
                .compose(RxUtils.silentScheduler())
                .map(RxUtils.bodyConverter())
                .subscribe(new SilentObserver<SessionBean>() {
                    @Override
                    public void onNext(@NonNull SessionBean sessionBean) {
                        loadHistory(0);
                    }
                });
    }

    public void addHistoryMessage(List<IMMessage> messages) {
        long last = 0;
        if (!messageTreeMap.isEmpty()) {
            last = messageTreeMap.lastKey();
        }
        for (IMMessage message : messages) {
            GlideIM.getDataStorage().storeMessage(message);
            messageTreeMap.put(message.getMid(), message);
        }
        if (!messageTreeMap.isEmpty() && messageTreeMap.lastKey() != last) {
            setLastMessage(messageTreeMap.lastEntry().getValue());
        }
        onSessionUpdate();
    }

    public void addHistoryMessage(IMMessage msg) {

    }

    private void onInsertMessage(IMMessage m) {

    }

    void onOfflineMessage(List<IMMessage> msg) {
        for (IMMessage m : msg) {
            GlideIM.getDataStorage().storeMessage(m);
            if (m.getMid() > lastReadMid) {
                onNewMessage(m);
            } else {
                messageChangeListener.onInsertMessage(m.getMid(), m);
                messageTreeMap.put(m.getMid(), m);
            }
        }
    }

    void onNewMessage(IMMessage msg) {
        unread++;
        setUpdateAt(msg.getSendAt());
        SLogger.d(TAG, "onNewMessage:" + msg);
        long mid = msg.getMid();
        long last = 0;
        if (!messageTreeMap.isEmpty()) {
            last = messageTreeMap.lastKey();
        }
        messageTreeMap.put(mid, msg);
        if (last < mid) {
            setLastMessage(msg);
        }
        onSessionUpdate();
        if (messageChangeListener != null) {
            messageChangeListener.onNewMessage(msg);
        }
    }


    private void onMessageSendSuccess(IMMessage message) {
        GlideIM.getDataStorage().storeMessage(message);
        onSessionUpdate();
    }

    private void onMessageSendFailed(IMMessage message) {
        GlideIM.getDataStorage().storeMessage(message);
        onSessionUpdate();
    }

    private void onMessageReceiveFailed(IMMessage message) {
        GlideIM.getDataStorage().storeMessage(message);

    }

    private void onMessageReceived(IMMessage message) {
        GlideIM.getDataStorage().storeMessage(message);

    }

    private void setLastMessage(IMMessage msg) {
        switch (msg.getType()) {
            case Constants.MESSAGE_TYPE_IMAGE:
                lastMsg = "[图片]";
                break;
            case Constants.MESSAGE_TYPE_VOICE:
                lastMsg = "[语音]";
                break;
            default:
                this.lastMsg = msg.getContent();
                break;
        }
        this.lastMsgId = msg.getMid();
        this.lastMsgSender = msg.getFrom();
        setUpdateAt(msg.getSendAt());
    }

    void setOnUpdateListener(SessionUpdateListener sessionUpdateListener) {
        this.sessionUpdateListener = sessionUpdateListener;
    }

    public void setMessageListener(MessageChangeListener l) {
        this.messageChangeListener = l;
    }

    public List<IMMessage> getMessages(long beforeMid, int maxLen) {
        List<IMMessage> ret = new ArrayList<>();
        if (messageTreeMap.isEmpty()) {
            return ret;
        }
        Long mid = beforeMid;
        if (mid == 0) {
            mid = messageTreeMap.lastKey();
        } else {
            mid = messageTreeMap.lowerKey(mid);
        }
        int count = maxLen;
        while (mid != null && count > 0) {
            IMMessage m = messageTreeMap.get(mid);
            ret.add(0, m);
            mid = messageTreeMap.lowerKey(mid);
            count--;
        }
        return ret;
    }

    void onDetailUpdated() {

    }

    private void onSessionUpdate() {
        if (sessionUpdateListener != null) {
            SLogger.d(TAG, "onUpdate");
            sessionUpdateListener.onUpdate(this);
        }
    }

    public void clearUnread() {
        if (unread == 0) {
            return;
        }
        unread = 0;
        if (messageTreeMap.size() > 0) {
            lastReadMid = messageTreeMap.lastKey();
        }
        onSessionUpdate();
    }

    public Single<List<IMMessage>> loadHistory(long beforeMid) {

        // return temp
        if (beforeMid == 0 && !messageTreeMap.isEmpty()) {
            Map.Entry<Long, IMMessage> entry = messageTreeMap.lastEntry();
            List<IMMessage> m = new ArrayList<>();
            while (entry != null && m.size() < 20) {
                m.add(entry.getValue());
                entry = messageTreeMap.lowerEntry(entry.getKey());
            }
            return Single.just(m);
        }

        switch (type) {
            case Constants.SESSION_TYPE_USER:
                GetChatHistoryDto getChatHistoryDto = new GetChatHistoryDto(to, beforeMid);
                return MsgApi.API.getChatMessageHistory(getChatHistoryDto)
                        .map(RxUtils.bodyConverter())
                        .flatMap((Function<List<MessageBean>, ObservableSource<MessageBean>>) Observable::fromIterable)
                        .map(messageBean -> IMMessage.fromMessage(account, messageBean))
                        .toList()
                        .doOnSuccess(this::addHistoryMessage);
            case Constants.SESSION_TYPE_GROUP:
                long seq = 0;
                if (beforeMid != 0) {
                    IMMessage m = messageTreeMap.get(beforeMid);
                    if (m != null) {
                        seq = m.getSeq();
                    }
                }
                GetGroupMsgHistoryDto dto = new GetGroupMsgHistoryDto(to, seq);
                return MsgApi.API.getGroupMessageHistory(dto)
                        .map(RxUtils.bodyConverter())
                        .flatMap((Function<List<GroupMessageBean>, ObservableSource<GroupMessageBean>>) Observable::fromIterable)
                        .map(messageBean -> IMMessage.fromGroupMessage(account, messageBean))
                        .toList()
                        .doOnSuccess(this::addHistoryMessage);
            default:
                return Single.error(new IllegalStateException("unknown session type " + type));
        }
    }

    public Observable<IMSession> initInfo() {
        if (infoInit) {
            return Observable.just(this);
        }
        switch (type) {
            case Constants.SESSION_TYPE_USER:
                return GlideIM.getUserInfo(to)
                        .map(this::setInfo);
            case Constants.SESSION_TYPE_GROUP:
                return GlideIM.getGroupInfo(to)
                        .map(this::setInfo)
                        .doOnError(throwable -> {
                            SLogger.e(TAG, throwable);
                        })
                        .onErrorReturn(throwable -> null);
            default:
                return Observable.just(this);
        }
    }

    public IMSession setInfo(GroupInfoBean groupInfoBean) {
        infoInit = true;
        to = groupInfoBean.getGid();
        title = groupInfoBean.getName();
        avatar = groupInfoBean.getAvatar();
        onSessionUpdate();
        return this;
    }

    public IMSession setInfo(UserInfoBean userInfoBean) {
        infoInit = true;
        to = userInfoBean.getUid();
        title = userInfoBean.getNickname();
        avatar = userInfoBean.getAvatar();
        onSessionUpdate();
        return this;
    }

    public void update(GroupMessageStateBean stateBean) {
        setUpdateAt(stateBean.getLastMsgAt());
    }

    public void update(SessionBean stateBean) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IMSession imSession = (IMSession) o;
        return to == imSession.to && type == imSession.type;
    }

    private Observable<ChatMessage> createMessage(int msgType, String msg) {
        return Observable.create(emitter -> {
            ChatMessage message = new ChatMessage();
            message.setMid(0);
            message.setContent(msg);
            message.setFrom(account.uid);
            message.setTo(to);
            message.setType(msgType);
            message.setState(ChatMessage.STATE_INIT);
            message.setcTime(System.currentTimeMillis() / 1000);
            emitter.onNext(message);
            emitter.onComplete();
        });
    }

    public Observable<IMMessage> sendTextMessage(String content) {
        return sendMessage(Constants.SESSION_TYPE_GROUP, content);
    }

    public Observable<IMMessage> sendMessage(int type, String content) {

        if (account.getIMClient() == null) {
            return Observable.error(new NullPointerException("the connection is not init"));
        }

        Observable<ChatMessage> creator = createMessage(type, content);
        Observable<MessageIDBean> midRequest = MsgApi.API.getMessageID()
                .map(RxUtils.bodyConverter());

        return creator
                .flatMap((Function<ChatMessage, ObservableSource<ChatMessage>>) message -> {
                    Observable<ChatMessage> init = Observable.just(message);
                    Observable<ChatMessage> create = Observable.just(message)
                            .zipWith(midRequest, (m1, messageIDBean) -> {
                                // set message id
                                m1.setState(ChatMessage.STATE_CREATED);
                                m1.setMid(messageIDBean.getMid());
                                return m1;
                            })
                            .flatMap((Function<ChatMessage, ObservableSource<ChatMessage>>) m2 -> {
                                // send message
                                Observable<ChatMessage> ob = send(m2);
                                return Observable.concat(Observable.just(m2), ob);
                            });
                    return Observable.concat(init, create);
                })
                .map(chatMessage -> {
                    IMMessage r;
                    if (chatMessage.getState() <= ChatMessage.STATE_CREATED) {
                        r = IMMessage.fromChatMessage(account, chatMessage);
                    } else {
                        r = getMessage(chatMessage.getMid());
                        r.setState(chatMessage.getState());
                    }

                    switch (chatMessage.getState()) {
                        case ChatMessage.STATE_INIT:
                            SLogger.d(TAG, "message initialized");
                            break;
                        case ChatMessage.STATE_RCV_SENDING:
                            SLogger.d(TAG, "resending message to receiver mid=" + r.getMid());
                            break;
                        case ChatMessage.STATE_CREATED:
                            onSendMessageCreated(r);
                            break;
                        case ChatMessage.STATE_SRV_RECEIVED:
                            onMessageSendSuccess(r);
                            break;
                        case ChatMessage.STATE_RCV_RECEIVED:
                            onMessageReceived(r);
                            break;
                        case ChatMessage.STATE_SRV_FAILED:
                            onMessageSendFailed(r);
                            break;
                        case ChatMessage.STATE_RCV_FAILED:
                            SLogger.d(TAG, "message send to receiver failed mid=" + r.getMid());
                            break;
                    }
                    return r;
                });
    }

    private Observable<ChatMessage> send(ChatMessage m) {
        IMClient im = account.getIMClient();
        if (im == null) {
            return Observable.error(new NullPointerException("the im connection is not init"));
        }
        switch (type) {
            case Constants.SESSION_TYPE_USER:
                return im.sendChatMessage(m);
            case Constants.SESSION_TYPE_GROUP:
                return im.sendGroupMessage(m);
            default:
                return Observable.error(new IllegalStateException("unknown session type"));
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(to, type);
    }


    public void setUpdateAt(long updateAt) {
        this.previousUpdateAt = this.updateAt;
        this.updateAt = updateAt;
    }

    @Override
    public String toString() {
        return "IMSession{" +
                "to=" + to +
                ", title='" + title + '\'' +
                ", avatar='" + avatar + '\'' +
                ", unread=" + unread +
                ", updateAt=" + updateAt +
                ", type=" + type +
                ", lastMsg='" + lastMsg + '\'' +
                ", lastMsgId=" + lastMsgId +
                ", messages=" + messageTreeMap +
                '}';
    }

    public interface SessionUpdateListener {
        void onUpdate(IMSession s);
    }

    public interface SessionDetailUpdateListener {
        void onUpdateUserInfo();
    }
}
