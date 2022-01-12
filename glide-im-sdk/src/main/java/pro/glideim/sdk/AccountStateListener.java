package pro.glideim.sdk;

public interface AccountStateListener {
    void onLoggedIn();

    void onReconnect();

    void onKickOut(String deviceInfo);
}
