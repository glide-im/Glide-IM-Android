package pro.glideim.sdk;

import java.util.List;

import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.entity.IMSession;

class DefaultDataStoreImpl implements DataStorage{

    private String token = "";


    @Override
    public void storeToken(long uid, String token) {
        this.token = token;
    }

    @Override
    public String loadToken(long uid) {
        return token;
    }

    @Override
    public void storeTempUserInfo(UserInfoBean userInfoBean) {

    }

    @Override
    public List<UserInfoBean> loadTempUserInfo() {
        return null;
    }

    @Override
    public void storeSession(long uid, IMSession session) {

    }

    @Override
    public List<IMSession> loadSessions(long uid) {
        return null;
    }
}
