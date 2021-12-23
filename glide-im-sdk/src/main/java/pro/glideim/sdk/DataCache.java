package pro.glideim.sdk;

import java.util.List;

import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.user.UserInfoBean;

public interface DataCache {
    void setUserInfo(List<UserInfoBean> userInfoBean);

    List<UserInfoBean> getUserInfo(long... uid);

    void setGroupInfo(List<GroupInfoBean> groupInfoBeans);

    List<GroupInfoBean> getGroupInfo(long... gid);

    void setToken(long uid, String token);

    String getToken(long uid);
}
