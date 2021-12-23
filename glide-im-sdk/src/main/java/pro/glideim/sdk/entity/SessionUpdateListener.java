package pro.glideim.sdk.entity;

public interface SessionUpdateListener {
    void onUpdate(IMSession session);

    void onNewSession(IMSession... session);
}