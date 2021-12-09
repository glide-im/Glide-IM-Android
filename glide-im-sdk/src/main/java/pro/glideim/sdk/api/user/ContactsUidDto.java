package pro.glideim.sdk.api.user;

public class ContactsUidDto {
    private long uid;
    private String remark;

    public ContactsUidDto(long uid, String remark) {
        this.uid = uid;
        this.remark = remark;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
