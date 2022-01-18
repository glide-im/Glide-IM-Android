package pro.glideim.sdk;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.msg.GetChatHistoryDto;
import pro.glideim.sdk.api.msg.GetGroupMsgHistoryDto;
import pro.glideim.sdk.api.msg.GroupMessageBean;
import pro.glideim.sdk.api.msg.GroupMessageStateBean;
import pro.glideim.sdk.api.msg.MessageBean;
import pro.glideim.sdk.api.msg.MessageIDBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.api.msg.SessionBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.im.IMClient;
import pro.glideim.sdk.protocol.ChatMessage;
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

    IMSessionList.SessionTag tag;
    private OnUpdateListener onUpdateListener;
    private MessageChangeListener messageChangeListener;
    private boolean infoInit = false;
    private long lastReadMid = 0;

    private IMSession(IMAccount account, long to, int type) {
        this.tag = IMSessionList.SessionTag.get(type, to);
        this.to = to;
        this.type = type;
        this.title = String.valueOf(to);
        this.avatar = "";
        setUpdateAt(System.currentTimeMillis() / 1000);
        this.account = account;
    }

    public static IMSession fromGroupState(IMAccount account, GroupMessageStateBean stateBean) {
        IMSession s = new IMSession(account, stateBean.getGid(), Constants.SESSION_TYPE_GROUP);
        s.unread = 0;
        s.setUpdateAt(stateBean.getLastMsgAt());
        s.lastMsgId = stateBean.getLastMID();
        return s;
    }

    public static IMSession fromSessionBean(IMAccount account, SessionBean sessionBean) {
        IMSession s;
        if (sessionBean.getUid1() == account.uid) {
            s = new IMSession(account, sessionBean.getUid2(), Constants.SESSION_TYPE_USER);
        } else {
            s = new IMSession(account, sessionBean.getUid1(), Constants.SESSION_TYPE_USER);
        }
        s.setUpdateAt(sessionBean.getUpdateAt());
        s.lastMsgId = sessionBean.getLastMid();
        return s;
    }

    public static IMSession create(IMAccount account, long to, int type, IMSessionList imSessionList) {
        IMSession s = new IMSession(account, to, type);
        s.setIMSessionList(imSessionList);
        s.initTargetInfo();
        return s;
    }

    public static IMSession create(IMAccount account, IMSessionList.SessionTag t, IMSessionList imSessionList) {
        IMSession s = new IMSession(account, t.getId(), t.getType());
        s.setIMSessionList(imSessionList);
        s.initTargetInfo();
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
        messageTreeMap.put(msg.getMid(), msg);
        setLastMessage(msg);
        onSessionUpdate();
    }

    public void addMessages(List<IMMessage> messages) {
        long last = 0;
        if (!messageTreeMap.isEmpty()) {
            last = messageTreeMap.lastKey();
        }
        for (IMMessage message : messages) {
            messageTreeMap.put(message.getMid(), message);
            if (message.getMid() > last) {
                setLastMessage(message);
                last = message.getMid();
            }
        }
        onSessionUpdate();
    }

    public void addHistoryMessage(IMMessage msg) {

    }

    private void onInsertMessage(IMMessage m) {

    }

    void onOfflineMessage(List<IMMessage> msg) {
        for (IMMessage m : msg) {
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


    public void onMessageSendSuccess(IMMessage message) {
        onSessionUpdate();
    }

    public void onMessageReceived(IMMessage message) {

    }

    private void setLastMessage(IMMessage msg) {
        this.lastMsg = msg.getContent();
        this.lastMsgId = msg.getMid();
        this.lastMsgSender = msg.getFrom();
        setUpdateAt(msg.getSendAt());
    }

    void setIMSessionList(IMSessionList list) {
    }

    void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    public void setMessageListener(MessageChangeListener l) {
        this.messageChangeListener = l;
    }

    public void initTargetInfo() {
        switch (type) {
            case Constants.SESSION_TYPE_USER:
                GlideIM.getUserInfo(to)
                        .compose(RxUtils.silentScheduler())
                        .subscribe(new SilentObserver<UserInfoBean>() {
                            @Override
                            public void onNext(@NonNull UserInfoBean userInfoBean) {
                                setInfo(userInfoBean);
                            }
                        });
                break;
            case Constants.SESSION_TYPE_GROUP:
                GlideIM.getGroupInfo(to)
                        .compose(RxUtils.silentScheduler())
                        .subscribe(new SilentObserver<GroupInfoBean>() {
                            @Override
                            public void onNext(@NonNull GroupInfoBean groupInfoBean) {
                                setInfo(groupInfoBean);
                            }
                        });
                break;
        }
    }

    public List<IMMessage> getLatestMessage() {
        return getMessages(0, 20);
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

    public List<IMMessage> getLatest() {
        return getMessages(0, 20);
    }

    private void onSessionUpdate() {
        long lastUpdateAt = System.currentTimeMillis();
//        if (latestMessage.size() > 0) {
//            IMMessage msg = latestMessage.get(latestMessage.size() - 1);
//            lastMsg = msg.getContent();
//            lastMsgId = msg.getMid();
//            lastMsgSender = msg.getFrom();
//        }

        if (onUpdateListener != null) {
            SLogger.d(TAG, "onUpdate");
            onUpdateListener.onUpdate(this);
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

    public Single<List<IMMessage>> getHistory(long beforeMid) {
        switch (type) {
            case Constants.SESSION_TYPE_USER:
                GetChatHistoryDto getChatHistoryDto = new GetChatHistoryDto(to, beforeMid);
                return MsgApi.API.getChatMessageHistory(getChatHistoryDto)
                        .map(RxUtils.bodyConverter())
                        .flatMap((Function<List<MessageBean>, ObservableSource<MessageBean>>) Observable::fromIterable)
                        .map(messageBean -> IMMessage.fromMessage(account, messageBean))
                        .toList()
                        .doOnSuccess(this::addMessages);
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
                        .doOnSuccess(this::addMessages);
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
                return GlideIM.getUserInfo(to).map(this::setInfo);
            case Constants.SESSION_TYPE_GROUP:
                return GlideIM.getGroupInfo(to).map(this::setInfo);
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

    public Observable<IMMessage> sendTextMessage(String msg) {
        if (account.getIMClient() == null) {
            return Observable.error(new NullPointerException("the connection is not init"));
        }

        Observable<ChatMessage> creator = createMessage(1, msg);
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

    public interface OnUpdateListener {
        void onUpdate(IMSession s);
    }
}
