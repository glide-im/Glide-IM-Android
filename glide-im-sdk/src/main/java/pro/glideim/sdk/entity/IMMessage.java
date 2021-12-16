package pro.glideim.sdk.entity;

import pro.glideim.sdk.GlideIM;
import pro.glideim.sdk.api.msg.GroupMessageBean;
import pro.glideim.sdk.api.msg.MessageBean;

public class IMMessage {
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

    private IMMessage() {
    }

    public static IMMessage fromMessage(MessageBean messageBean) {
        IMMessage m = new IMMessage();
        m.setMid(messageBean.getMid());
        m.setCliSeq(messageBean.getCliSeq());
        m.setFrom(messageBean.getFrom());
        m.setTo(messageBean.getTo());
        m.setType(m.getType());
        m.setSendAt(messageBean.getSendAt());
        m.setCreateAt(messageBean.getCreateAt());
        m.setContent(messageBean.getContent());
        m.targetType = 1;
        if (m.from == GlideIM.getInstance().getMyUID()) {
            m.targetId = m.to;
        } else {
            m.targetId = m.from;
        }
        return m;
    }

    public static IMMessage fromGroupMessage(GroupMessageBean messageBean) {
        IMMessage m = new IMMessage();
        m.setMid(messageBean.getMid());
        m.setCliSeq(0);
        m.setFrom(messageBean.getSender());
        m.setTo(0);
        m.setType(m.getType());
        m.setSendAt(messageBean.getSentAt());
        m.setContent(messageBean.getContent());
        m.targetType = 2;
        m.targetId = m.to;
        return m;
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

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public void setTo(long to) {
        this.to = to;
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
                ", cliSeq=" + cliSeq +
                ", from=" + from +
                ", to=" + to +
                ", type=" + type +
                ", sendAt=" + sendAt +
                ", createAt=" + createAt +
                ", content='" + content + '\'' +
                '}';
    }
}
