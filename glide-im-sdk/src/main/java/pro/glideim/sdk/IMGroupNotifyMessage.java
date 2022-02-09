package pro.glideim.sdk;

import pro.glideim.sdk.messages.GroupNotify;
import pro.glideim.sdk.messages.GroupNotifyMemberChanges;

public class IMGroupNotifyMessage extends IMMessage {

    public GroupNotify<GroupNotifyMemberChanges> notify;

    public IMGroupNotifyMessage(IMAccount account, GroupNotify<GroupNotifyMemberChanges> notify) {
        super(account);
        this.notify = notify;
        this.setMid(notify.getMid());
        this.setCliSeq(0);
        this.setFrom(0);
        this.setTo(notify.getGid());
        this.setSeq(notify.getSeq());
        this.setType(Constants.MESSAGE_TYPE_GROUP_NOTIFY);
        this.setSendAt(notify.getTimestamp());
        this.setContent("-");
        this.setTarget(Constants.SESSION_TYPE_GROUP, notify.getGid());
    }
}
