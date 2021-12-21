package pro.glideim.sdk.entity;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import pro.glideim.sdk.GlideIM;
import pro.glideim.sdk.SilentObserver;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.msg.GroupMessageBean;
import pro.glideim.sdk.api.msg.GroupMessageStateBean;
import pro.glideim.sdk.api.msg.SessionBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.utils.RxUtils;

public class IMSession {

//    private final List<IMMessage> latestMessage = new ArrayList<>();

    private final TreeMap<Long, IMMessage> latestMessageMap = new TreeMap<>();
    private final IMSessionMessage messages;
    public long to;
    public long lastMsgSender;
    public String title;
    public String avatar;
    public int unread;
    public long updateAt;
    public int type;
    public String lastMsg;
    public long lastMsgId;
    private long lastUpdateAt;

    private IMSessionList sessionList;
    private OnUpdateListener onUpdateListener;

    private IMSession() {
        messages = new IMSessionMessage(this);
    }

    public static IMSession fromGroupState(GroupMessageStateBean stateBean) {
        IMSession s = new IMSession();
        s.to = stateBean.getGid();
        s.unread = 0;
        s.updateAt = stateBean.getLastMsgAt();
        s.type = 2;
        s.lastMsgId = stateBean.getLastMID();
        return s;
    }

    public static IMSession fromSessionBean(Long myUid, SessionBean sessionBean) {
        IMSession s = new IMSession();
        if (sessionBean.getUid1() == myUid) {
            s.to = sessionBean.getUid2();
        } else {
            s.to = sessionBean.getUid1();
        }
        s.type = 1;
        s.updateAt = sessionBean.getUpdateAt();
        s.lastMsgId = sessionBean.getLastMid();
        return s;
    }

    public static IMSession fromIMMessage(IMMessage message) {
        IMSession s = new IMSession();
        s.type = message.getTargetType();
        s.to = message.getTo();
        s.updateAt = message.getSendAt();
        s.lastMsgId = message.getMid();
        s.lastMsg = message.getContent();
        s.initTargetInfo();
        return s;
    }

    public static IMSession create(long to, int type) {
        IMSession s = new IMSession();
        s.to = to;
        s.type = type;
        return s;
    }

    public IMSessionMessage getMessages() {
        return this.messages;
    }

    public IMSession update(IMSession session) {
        this.updateAt = session.updateAt;
        this.lastMsg = session.lastMsg;
        return this;
    }

    public void addMessage(IMMessage message) {
        messages.addMessage(message);
        onUpdate();
    }

    public void onMessageSendSuccess(IMMessage message) {

    }

    public void onMessageReceived(IMMessage message) {

    }

    void setIMSessionList(IMSessionList list) {
        sessionList = list;
    }

    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    public void initTargetInfo() {
        switch (type) {
            case 1:
                GlideIM.getUserInfo(to)
                        .compose(RxUtils.silentScheduler())
                        .subscribe(new SilentObserver<UserInfoBean>() {
                            @Override
                            public void onNext(@NonNull UserInfoBean userInfoBean) {
                                setUserInfo(userInfoBean);
                            }
                        });
                break;
            case 2:
                GlideIM.getGroupInfo(to)
                        .compose(RxUtils.silentScheduler())
                        .subscribe(new SilentObserver<GroupInfoBean>() {
                            @Override
                            public void onNext(@NonNull GroupInfoBean groupInfoBean) {
                                setGroupInfo(groupInfoBean);
                            }
                        });
                break;
        }
    }

    public void addGroupMessage(List<GroupMessageBean> message) {
        for (GroupMessageBean messageBean : message) {
            IMMessage imMessage = IMMessage.fromGroupMessage(messageBean);
            messages.addMessage(imMessage);
        }
    }

    public List<IMMessage> getLatestMessage() {
        return messages.getLatest();
    }

    public void addMessages(List<IMMessage> message) {
        messages.addMessages(message);
        onUpdate();
    }

    public IMSession setGroupInfo(GroupInfoBean groupInfoBean) {
        to = groupInfoBean.getGid();
        title = groupInfoBean.getName();
        avatar = groupInfoBean.getAvatar();
        onUpdate();
        return this;
    }

    public IMSession setUserInfo(UserInfoBean userInfoBean) {
        to = userInfoBean.getUid();
        title = userInfoBean.getNickname();
        avatar = userInfoBean.getAvatar();
        onUpdate();
        return this;
    }

    private void onUpdate() {
        lastUpdateAt = System.currentTimeMillis();
//        if (latestMessage.size() > 0) {
//            IMMessage msg = latestMessage.get(latestMessage.size() - 1);
//            lastMsg = msg.getContent();
//            lastMsgId = msg.getMid();
//            lastMsgSender = msg.getFrom();
//        }

        if (onUpdateListener != null) {
            onUpdateListener.onUpdate();
        }
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
                ", latestMessage=" + messages.getLatest() +
                '}';
    }

    public interface OnUpdateListener {
        void onUpdate();
    }
}
