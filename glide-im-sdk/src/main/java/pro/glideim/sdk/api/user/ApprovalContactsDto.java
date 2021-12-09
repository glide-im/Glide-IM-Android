package pro.glideim.sdk.api.user;

public class ApprovalContactsDto {
    private long uid;
    private boolean agree;
    private boolean deny;
    private String comment;

    public ApprovalContactsDto(long uid, boolean agree, boolean deny, String comment) {
        this.uid = uid;
        this.agree = agree;
        this.deny = deny;
        this.comment = comment;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public boolean isAgree() {
        return agree;
    }

    public void setAgree(boolean agree) {
        this.agree = agree;
    }

    public boolean isDeny() {
        return deny;
    }

    public void setDeny(boolean deny) {
        this.deny = deny;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
