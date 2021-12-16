package pro.glideim.sdk;

import java.util.List;

import pro.glideim.sdk.api.user.UserInfoBean;

public interface DataStorage {
    void storeToken(String token);

    void storeTempUserInfo(UserInfoBean userInfoBean);

    String loadToken();

    List<UserInfoBean> loadTempUserInfo();
}
