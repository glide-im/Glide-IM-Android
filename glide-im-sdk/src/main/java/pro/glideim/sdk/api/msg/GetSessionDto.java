package pro.glideim.sdk.api.msg;

public class GetSessionDto {
    private long to;

    public GetSessionDto(long to) {
        this.to = to;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }
}
