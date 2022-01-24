package pro.glideim.sdk;

import java.util.List;

import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.user.UserInfoBean;

public interface DataStorage {

    long getDefaultAccountUid();

    void storeToken(long uid, String token);

    String loadToken(long uid);

    void storeTempUserInfo(UserInfoBean userInfoBean);

    UserInfoBean loadTempUserInfo(long uid);

    void storeTempGroupInfo(GroupInfoBean groupInfoBean);

    GroupInfoBean loadTempGroupInfo(long gid);

    void storeSession(long uid, IMSession session);

    List<IMSession> loadSessions(long uid);

    List<IMMessage> loadMessage(long uid, int type, long to);

    void storeMessage(IMMessage message);
}
