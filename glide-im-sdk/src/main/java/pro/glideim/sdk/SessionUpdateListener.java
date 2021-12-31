package pro.glideim.sdk;

public interface SessionUpdateListener {
    void onUpdate(IMSession session);

    void onNewSession(IMSession session);
}