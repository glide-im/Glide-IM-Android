package pro.glideim.sdk.messages;

import java.lang.reflect.Type;

import pro.glideim.sdk.http.RetrofitManager;

public class CommMessage<T> {
    private int ver;
    private String action;
    private long seq;
    private T data;
    private String origin;

    public CommMessage(int ver, String action, long seq, T data) {
        this.ver = ver;
        this.action = action;
        this.seq = seq;
        this.data = data;
    }

    public <R> CommMessage<R> deserialize(Type t) {
        if (origin.isEmpty()) {
            throw new IllegalStateException("the common message origin data is empty");
        }
        return RetrofitManager.fromJson(t, origin);
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public boolean success() {
        return action.equals(Actions.Srv.ACTION_API_SUCCESS);
    }

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CommMessage{" +
                "ver=" + ver +
                ", action='" + action + '\'' +
                ", seq=" + seq +
                ", data=" + data +
                '}';
    }
}
