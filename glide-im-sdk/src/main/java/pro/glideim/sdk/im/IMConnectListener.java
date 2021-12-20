package pro.glideim.sdk.im;

public interface IMConnectListener {
    void onError(Throwable t);
    void onSuccess();
}
