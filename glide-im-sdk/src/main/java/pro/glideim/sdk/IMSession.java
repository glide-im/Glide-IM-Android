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
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.api.msg.SessionBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.utils.RxUtils;
import pro.glideim.sdk.utils.SLogger;

public class IMSession {

    public static final String TAG = IMSession.class.getSimpleName();
    private final TreeMap<Long, IMMessage> messageTreeMap = new TreeMap<>();
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
    private IMAccount account;

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

    public IMSession update(IMSession session) {
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

    public IMSession setInfo(GroupInfoBean groupInfoBean) {
        to = groupInfoBean.getGid();
        title = groupInfoBean.getName();
        avatar = groupInfoBean.getAvatar();
        onSessionUpdate();
        return this;
    }

    public IMSession setInfo(UserInfoBean userInfoBean) {
        to = userInfoBean.getUid();
        title = userInfoBean.getNickname();
        avatar = userInfoBean.getAvatar();
        onSessionUpdate();
        return this;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IMSession imSession = (IMSession) o;
        return to == imSession.to && type == imSession.type;
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
