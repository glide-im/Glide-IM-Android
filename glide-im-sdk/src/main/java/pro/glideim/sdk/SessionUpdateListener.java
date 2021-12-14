package pro.glideim.sdk;

import java.util.List;

import pro.glideim.sdk.entity.IMSession;

public interface SessionUpdateListener {
    void onUpdate(List<IMSession> sessions);

    void onError(Throwable t);
}