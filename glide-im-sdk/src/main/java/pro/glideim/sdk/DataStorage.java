package pro.glideim.sdk;

import java.util.List;

import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.entity.IMSession;

public interface DataStorage {
    void storeToken(long uid, String token);

    String loadToken(long uid);

    void storeTempUserInfo(UserInfoBean userInfoBean);

    List<UserInfoBean> loadTempUserInfo();

    void storeSession(long uid, IMSession session);

    List<IMSession> loadSessions(long uid);

}
