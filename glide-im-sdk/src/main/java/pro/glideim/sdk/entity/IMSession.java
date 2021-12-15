package pro.glideim.sdk.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import pro.glideim.sdk.GlideIM;
import pro.glideim.sdk.SilentObserver;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.msg.GroupMessageBean;
import pro.glideim.sdk.api.msg.GroupMessageStateBean;
import pro.glideim.sdk.api.msg.SessionBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.utils.RxUtils;

public class IMSession {
    public long to;
    public String title;
    public String avatar;
    public int unread;
    public long updateAt;
    public int type;
    public String lastMsg;
    public String lastMsgTitle;
    public long lastMsgId;

    public List<IMMessage> latestMessage = new ArrayList<>();

    private OnUpdateListener onUpdateListener;

    public static IMSession fromGroupState(GroupMessageStateBean stateBean) {
        IMSession s = new IMSession();
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
        s.updateAt = sessionBean.getUpdateAt();
        s.lastMsgId = sessionBean.getLastMid();
        return s;
    }

    public static IMSession create(long to, int type) {
        IMSession s = new IMSession();
        s.to = to;
        s.type = type;
        return s;
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

    public IMSession setLatestGroupMessage(List<GroupMessageBean> message) {
        latestMessage.clear();
        for (GroupMessageBean messageBean : message) {
            IMMessage imMessage = IMMessage.fromGroupMessage(messageBean);
            latestMessage.add(imMessage);
        }
        return this;
    }

    public IMSession setLatestMessage(List<IMMessage> message) {
        latestMessage.clear();
        latestMessage.addAll(message);
        onUpdate();
        return this;
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
        if (onUpdateListener != null) {
            onUpdateListener.onUpdate();
        }
    }

    @Override
    public String toString() {
        return "IMSession{" +
                "title='" + title + '\'' +
                ", avatar='" + avatar + '\'' +
                ", unread=" + unread +
                ", updateAt=" + updateAt +
                ", type=" + type +
                ", lastMsg='" + lastMsg + '\'' +
                ", lastMsgTitle='" + lastMsgTitle + '\'' +
                '}';
    }

    public interface OnUpdateListener {
        void onUpdate();
    }
}
