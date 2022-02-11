package pro.glideim.sdk;

import androidx.annotation.NonNull;

import java.util.Objects;

import pro.glideim.sdk.api.msg.GroupMessageBean;
import pro.glideim.sdk.api.msg.MessageBean;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.messages.ChatMessage;
import pro.glideim.sdk.messages.GroupMessage;
import pro.glideim.sdk.utils.RxUtils;

public class IMMessage {

    public static final int STATUS_RECALLED = 1;
    public static final int STATUS_DISABLED = 2;

    private final long accountUid;
    public String avatar;
    public String title;
    IMSessionList.SessionTag tag;
    private long mid;
    private long cliSeq;
    private long from;
    private long to;
    private int type;
    private long sendAt;
    private long createAt;
    private String content;
    private long targetId;
    private int targetType;
    private int state;
    private int status;
    private long recallBy;
    private long seq;

    public IMMessage(IMAccount account) {
        accountUid = account.uid;
    }

    public static IMMessage fromChatMessage(IMAccount account, ChatMessage message) {
        IMMessage m = new IMMessage(account);
        m.setMid(message.getMid());
        m.setCliSeq(message.getcSeq());
        m.setFrom(message.getFrom());
        m.setTo(message.getTo());
        m.setType(message.getType());
        m.setSendAt(message.getcTime());
        m.setCreateAt(message.getcTime());
        m.setContent(message.getContent());
        m.setState(message.getState());
        m.setState(message.getStatus());
        long id;
        if (m.from == account.uid) {
            id = m.to;
        } else {
            id = m.from;
        }
        m.setTarget(Constants.SESSION_TYPE_USER, id);
        return m;
    }

    public static IMMessage fromMessage(IMAccount account, MessageBean messageBean) {
        IMMessage m = new IMMessage(account);
        m.setMid(messageBean.getMid());
        m.setCliSeq(messageBean.getCliSeq());
        m.setFrom(messageBean.getFrom());
        m.setTo(messageBean.getTo());
        m.setType(messageBean.getType());
        m.setSendAt(messageBean.getSendAt());
        m.setCreateAt(messageBean.getCreateAt());
        m.setContent(messageBean.getContent());
        m.setStatus(messageBean.getStatus());
        long id = 0;
        if (m.from == account.uid) {
            id = m.to;
        } else {
            id = m.from;
        }
        m.setTarget(Constants.SESSION_TYPE_USER, id);
        return m;
    }

    public static IMMessage fromGroupMessage(IMAccount account, GroupMessageBean messageBean) {
        IMMessage m = new IMMessage(account);
        m.setMid(messageBean.getMid());
        m.setCliSeq(0);
        m.setFrom(messageBean.getSender());
        m.setTo(messageBean.getGid());
        m.setSeq(messageBean.getSeq());
        m.setType(messageBean.getType());
        m.setSendAt(messageBean.getSendAt());
        m.setContent(messageBean.getContent());
        m.setStatus(messageBean.getStatus());
        m.setRecallBy(messageBean.getRecallBy());
        m.setTarget(Constants.SESSION_TYPE_GROUP, m.to);
        return m;
    }

    public static IMMessage fromGroupMessage(IMAccount account, GroupMessage message) {
        IMMessage m = new IMMessage(account);
        m.setMid(message.getMid());
        m.setCliSeq(0);
        m.setFrom(message.getFrom());
        m.setTo(message.getTo());
        m.setSeq(message.getSeq());
        m.setType(message.getType());
        m.setSendAt(message.getSendAt());
        m.setContent(message.getContent());
        m.setStatus(message.getStatus());
        m.setTarget(Constants.SESSION_TYPE_GROUP, m.to);
        return m;
    }

    protected void setTarget(int type, long id) {
        this.targetType = type;
        this.targetId = id;
        this.tag = IMSessionList.SessionTag.get(type, id);

        long uid = id;
        if (type == Constants.SESSION_TYPE_GROUP) {
            uid = from;
        }
        GlideIM.getUserInfo(uid)
                .compose(RxUtils.silentSchedulerSingle())
                .subscribe(new SilentObserver<UserInfoBean>() {
                    @Override
                    public void onNext(@NonNull UserInfoBean userInfoBean) {
                        avatar = userInfoBean.getAvatar();
                        title = userInfoBean.getNickname();
                    }
                });
    }

    public boolean isVisible() {
        return getType() != Constants.MESSAGE_TYPE_GROUP_NOTIFY
                && getType() != Constants.MESSAGE_TYPE_RECALL;
    }

    public long getRecallBy() {
        return recallBy;
    }

    public void setRecallBy(long recallBy) {
        this.recallBy = recallBy;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public boolean isSendSuccess() {
        return getState() == ChatMessage.STATE_SRV_RECEIVED;
    }

    public boolean isSending() {
        return getState() == ChatMessage.STATE_SRV_SENDING;
    }

    public boolean isSendFailed() {
        return getState() == ChatMessage.STATE_SRV_FAILED;
    }

    public boolean isReceived() {
        return getState() == ChatMessage.STATE_RCV_RECEIVED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IMMessage imMessage = (IMMessage) o;
        return imMessage.mid == this.mid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isMe() {
        return accountUid == from;
    }

    public IMSessionList.SessionTag getTag() {
        return tag;
    }

    public void setTag(IMSessionList.SessionTag tag) {
        this.tag = tag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mid);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public long getMid() {
        return mid;
    }

    public void setMid(long mid) {
        this.mid = mid;
    }

    public long getCliSeq() {
        return cliSeq;
    }

    public void setCliSeq(long cliSeq) {
        this.cliSeq = cliSeq;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getSendAt() {
        return sendAt;
    }

    public void setSendAt(long sendAt) {
        this.sendAt = sendAt;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "IMMessage{" +
                "mid=" + mid +
                ", from=" + from +
                ", to=" + to +
                ", cliSeq=" + cliSeq +
                ", type=" + type +
                ", sendAt=" + sendAt +
                ", createAt=" + createAt +
                ", content='" + content + '\'' +
                '}';
    }
}
