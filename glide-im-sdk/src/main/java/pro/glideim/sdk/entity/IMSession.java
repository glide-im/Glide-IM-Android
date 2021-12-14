package pro.glideim.sdk.entity;

import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.msg.GroupMessageStateBean;
import pro.glideim.sdk.api.msg.SessionBean;
import pro.glideim.sdk.api.user.UserInfoBean;

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

    public IMSession setGroupInfo(GroupInfoBean groupInfoBean) {
        to = groupInfoBean.getGid();
        title = groupInfoBean.getName();
        avatar = groupInfoBean.getAvatar();
        return this;
    }

    public IMSession setUserInfo(UserInfoBean userInfoBean) {
        to = userInfoBean.getUid();
        title = userInfoBean.getNickname();
        avatar = userInfoBean.getAvatar();
        return this;
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
}
