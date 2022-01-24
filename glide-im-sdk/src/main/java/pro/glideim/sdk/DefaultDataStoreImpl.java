package pro.glideim.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.user.UserInfoBean;

public class DefaultDataStoreImpl implements DataStorage {


    private static final Map<Long, UserInfoBean> sTempUserInfo = new HashMap<>();
    private static final Map<Long, GroupInfoBean> sTempGroupInfo = new HashMap<>();
    private static final Map<Long, HashMap<Long, IMSession>> sTempSession = new HashMap<>();

    private String token = "";
    private long uid = 0;

    @Override
    public long getDefaultAccountUid() {
        return uid;
    }

    @Override
    public void storeToken(long uid, String token) {
        this.uid = uid;
        this.token = token;
    }

    @Override
    public String loadToken(long uid) {
        return token;
    }

    @Override
    public void storeTempUserInfo(UserInfoBean userInfoBean) {
        sTempUserInfo.put(userInfoBean.getUid(), userInfoBean);
    }

    @Override
    public UserInfoBean loadTempUserInfo(long uid) {
        return sTempUserInfo.get(uid);
    }

    @Override
    public void storeTempGroupInfo(GroupInfoBean groupInfoBean) {
        sTempGroupInfo.put(groupInfoBean.getGid(), groupInfoBean);
    }

    @Override
    public GroupInfoBean loadTempGroupInfo(long gid) {
        return sTempGroupInfo.get(gid);
    }

    @Override
    public void storeSession(long uid, IMSession session) {
        if (!sTempSession.containsKey(uid)) {
            sTempSession.put(uid, new HashMap<>());
        }
        sTempSession.get(uid).put(session.to, session);
    }

    @Override
    public List<IMSession> loadSessions(long uid) {
        if (!sTempSession.containsKey(uid)) {
            sTempSession.put(uid, new HashMap<>());
        }
        return new ArrayList<>(sTempSession.get(uid).values());
    }

    @Override
    public List<IMMessage> loadMessage(long uid, int type, long to) {
        return new ArrayList<>();
    }

    @Override
    public void storeMessage(IMMessage message) {

    }
}
