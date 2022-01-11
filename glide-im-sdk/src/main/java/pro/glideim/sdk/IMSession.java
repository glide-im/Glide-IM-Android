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
import pro.glideim.sdk.api.msg.GroupMessageStateBean;
import pro.glideim.sdk.api.msg.MessageBean;
import pro.glideim.sdk.api.msg.MessageIDBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.api.msg.SessionBean;
import pro.glideim.sdk.api.user.UserInfoBean;
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
    public int type;
    public String lastMsg;
    public long lastMsgId;
    IMSessionList.SessionTag tag;
    private long lastUpdateAt;
    private IMSessionList sessionList;
    private OnUpdateListener onUpdateListener;
    private MessageChangeListener messageChangeListener;
    private boolean infoInit = false;

    private IMSession(IMAccount account, long to, int type) {
        this.tag = IMSessionList.SessionTag.get(type, to);
        this.to = to;
        this.type = type;
        this.updateAt = System.currentTimeMillis() / 1000;
        this.account = account;
    }

    public static IMSession fromGroupState(IMAccount account, GroupMessageStateBean stateBean) {
        IMSession s = new IMSession(account, stateBean.getGid(), 2);
        s.unread = 0;
        s.updateAt = stateBean.getLastMsgAt();
        s.lastMsgId = stateBean.getLastMID();
        return s;
    }

    public static IMSession fromSessionBean(IMAccount account, SessionBean sessionBean) {
        IMSession s;
        if (sessionBean.getUid1() == account.uid) {
            s = new IMSession(account, sessionBean.getUid2(), 1);
        } else {
            s = new IMSession(account, sessionBean.getUid1(), 1);
        }
        s.updateAt = sessionBean.getUpdateAt();
        s.lastMsgId = sessionBean.getLastMid();
        return s;
    }

    public static IMSession fromIMMessage(IMAccount account, IMMessage message) {
        IMSession s = new IMSession(account, message.getTo(), message.getTargetType());
        s.updateAt = message.getSendAt();
        s.lastMsgId = message.getMid();
        s.lastMsg = message.getContent();
        s.initTargetInfo();
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
        this.updateAt = session.updateAt;
        this.lastMsg = session.lastMsg;
        this.messageTreeMap.putAll(session.messageTreeMap);
        return this;
    }

    public void addMessage(IMMessage msg) {
        updateAt = msg.getSendAt();
        SLogger.d(TAG, "addMessage:" + msg);
        // TODO cache msg to file
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

    public void onNewMessage(IMMessage m) {
        addMessage(m);
        if (messageChangeListener != null) {
            messageChangeListener.onNewMessage(m);
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
        this.updateAt = msg.getSendAt();
    }

    void setIMSessionList(IMSessionList list) {
        sessionList = list;
    }

    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }


    public void setMessageListener(MessageChangeListener l) {
        this.messageChangeListener = l;
    }

    public void initTargetInfo() {
        switch (type) {
            case 1:
                GlideIM.getUserInfo(to)
                        .compose(RxUtils.silentScheduler())
                        .subscribe(new SilentObserver<UserInfoBean>() {
                            @Override
                            public void onNext(@NonNull UserInfoBean userInfoBean) {
                                setInfo(userInfoBean);
                            }
                        });
                break;
            case 2:
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
        lastUpdateAt = System.currentTimeMillis();
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

    public Single<List<IMMessage>> getHistory(long beforeMid) {
        GetChatHistoryDto getChatHistoryDto = new GetChatHistoryDto(to, beforeMid);
        return MsgApi.API.getChatMessageHistory(getChatHistoryDto)
                .map(RxUtils.bodyConverter())
                .flatMap((Function<List<MessageBean>, ObservableSource<MessageBean>>) Observable::fromIterable)
                .map(messageBean -> IMMessage.fromMessage(account, messageBean))
                .toList()
                .doOnSuccess(this::addMessages);
    }

    public Observable<IMSession> initInfo() {
        if (infoInit) {
            return Observable.just(this);
        }
        switch (type) {
            case 1:
                return GlideIM.getUserInfo(to).map(this::setInfo);
            case 2:
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


    public Observable<IMMessage> sendMessage(String msg) {
        if (account.getIMClient() == null) {
            return Observable.error(new NullPointerException("the connection is not init"));
        }

        Observable<ChatMessage> creator = Observable.create(emitter -> {
            ChatMessage message = new ChatMessage();
            message.setMid(0);
            message.setContent(msg);
            message.setFrom(account.uid);
            message.setTo(to);
            message.setType(type);
            message.setState(ChatMessage.STATE_INIT);
            message.setcTime(System.currentTimeMillis() / 1000);
            emitter.onNext(message);
            emitter.onComplete();
        });

        Observable<MessageIDBean> midRequest = MsgApi.API.getMessageID()
                .map(RxUtils.bodyConverter());

        return creator
                .flatMap((Function<ChatMessage, ObservableSource<ChatMessage>>) message -> {
                    Observable<ChatMessage> init = Observable.just(message);
                    Observable<ChatMessage> create = Observable.just(message)
                            .zipWith(midRequest, (m1, messageIDBean) -> {
                                m1.setState(ChatMessage.STATE_CREATED);
                                m1.setMid(messageIDBean.getMid());
                                return m1;
                            })
                            .flatMap((Function<ChatMessage, ObservableSource<ChatMessage>>) m2 -> {
                                Observable<ChatMessage> ob = account.getIMClient().sendChatMessage(m2);
                                return Observable.concat(Observable.just(m2), ob);
                            });
                    return Observable.concat(init, create);
                })
                .map(chatMessage -> {
                    IMMessage r = IMMessage.fromChatMessage(account, chatMessage);
                    IMSession session = sessionList.getSession(chatMessage.getType(), chatMessage.getTo());
                    switch (chatMessage.getState()) {
                        case ChatMessage.STATE_INIT:
                            break;
                        case ChatMessage.STATE_CREATED:
                            session.addMessage(r);
                            break;
                        case ChatMessage.STATE_SRV_RECEIVED:
                            session.onMessageSendSuccess(r);
                            break;
                        case ChatMessage.STATE_RCV_RECEIVED:
                            session.onMessageReceived(r);
                            break;
                    }
                    return r;
                });
    }

    @Override
    public int hashCode() {
        return Objects.hash(to, type);
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
