package pro.glideim.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.api.auth.AuthApi;
import pro.glideim.sdk.api.auth.AuthBean;
import pro.glideim.sdk.api.auth.LoginDto;
import pro.glideim.sdk.api.auth.RegisterDto;
import pro.glideim.sdk.api.group.CreateGroupBean;
import pro.glideim.sdk.api.group.CreateGroupDto;
import pro.glideim.sdk.api.group.GetGroupInfoDto;
import pro.glideim.sdk.api.group.GroupApi;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.msg.AckOfflineMsgDto;
import pro.glideim.sdk.api.msg.MessageBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.api.msg.SessionBean;
import pro.glideim.sdk.api.user.GetUserInfoDto;
import pro.glideim.sdk.api.user.ProfileBean;
import pro.glideim.sdk.api.user.UserApi;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.entity.IMAccount;
import pro.glideim.sdk.entity.IMMessage;
import pro.glideim.sdk.http.RetrofitManager;
import pro.glideim.sdk.im.IMClientImpl;
import pro.glideim.sdk.utils.RxUtils;

public class GlideIM {

    public static final String TAG = "GlideIM";
    private static final Map<Long, UserInfoBean> sTempUserInfo = new HashMap<>();
    private static final Map<Long, GroupInfoBean> sTempGroupInfo = new HashMap<>();
    private static final Map<String, SessionBean> sTempSession = new HashMap<>();
    private static GlideIM sInstance;
    public DataStorage dataStorage = new DefaultDataStoreImpl();
    private IMAccount account;

    private GlideIM() {
    }

    public static GlideIM getInstance() {
        return sInstance;
    }

    public static void init(String wsUrl, String baseUrlApi) {
        IMClientImpl im = IMClientImpl.create(wsUrl);
        RetrofitManager.init(baseUrlApi);
        sInstance = new GlideIM();
        sInstance.account = new IMAccount(im);
    }

    public static Observable<List<IMMessage>> subscribeChatMessageChanges(long to, int type) {
        return Observable.empty();
    }

    public static Observable<IMMessage> sendChatMessage(long to, int type, String content) {
        return getAccount().sendChatMessage(to, type, content);
    }

    public static Observable<ProfileBean> auth() {
        return getAccount().auth();
    }

    static Observable<Boolean> authWs() {
        return getAccount().authWs();
    }

    public static Observable<Boolean> login(String account, String password, int device) {
        return AuthApi.API.login(new LoginDto(account, password, device))
                .map(RxUtils.bodyConverter())
                .flatMap((Function<AuthBean, ObservableSource<Boolean>>) authBean -> {
                    getDataStorage().storeToken(getAccount().uid, authBean.getToken());
                    getAccount().uid = authBean.getUid();
                    return authWs();
                });
    }

    public static Observable<Boolean> register(String account, String password) {
        return AuthApi.API.register(new RegisterDto(account, password))
                .map(RxUtils.bodyConverter())
                .map(o -> true);
    }

    public static Observable<List<MessageBean>> getOfflineMessage() {
        return MsgApi.API.getOfflineMsg()
                .map(RxUtils.bodyConverter())
                .doOnNext(messageBeans -> {
                    List<Long> mids = new ArrayList<>();
                    for (MessageBean b : messageBeans) {
                        mids.add(b.getMid());
                    }
                    MsgApi.API.ackOfflineMsg(new AckOfflineMsgDto(mids)).subscribe(new SilentObserver<>());
                });
    }

    public static Observable<GroupInfoBean> getGroupInfo(long gid) {
        if (sTempGroupInfo.containsKey(gid)) {
            return Observable.just(sTempGroupInfo.get(gid));
        }
        return GroupApi.API.getGroupInfo(new GetGroupInfoDto(Collections.singletonList(gid)))
                .map(RxUtils.bodyConverter())
                .map(groupInfo -> {
                    GroupInfoBean g = groupInfo.get(0);
                    sTempGroupInfo.put(g.getGid(), g);
                    return g;
                });
    }

    public static Observable<List<GroupInfoBean>> getGroupInfo(List<Long> gid) {

        final List<GroupInfoBean> temped = new ArrayList<>();
        List<Long> filtered = new ArrayList<>();
        for (Long id : gid) {
            if (!sTempGroupInfo.containsKey(id)) {
                filtered.add(id);
            } else {
                temped.add(sTempGroupInfo.get(id));
            }
        }
        if (filtered.isEmpty()) {
            return Observable.just(temped);
        }
        Observable<Response<List<GroupInfoBean>>> s = GroupApi.API.getGroupInfo(new GetGroupInfoDto(filtered));
        return s.map(RxUtils.bodyConverter()).map(r -> {
            for (GroupInfoBean u : r) {
                sTempGroupInfo.put(u.getGid(), u);
            }
            temped.addAll(r);
            return r;
        });
    }

    public static GroupInfoBean getTempGroupInfo(long gid) {
        return sTempGroupInfo.get(gid);
    }

    public static UserInfoBean getTempUserInfo(long uid) {
        return sTempUserInfo.get(uid);
    }

    public static Observable<UserInfoBean> getUserInfo(long uid) {
        if (sTempUserInfo.containsKey(uid)) {
            return Observable.just(sTempUserInfo.get(uid));
        }
        return UserApi.API.getUserInfo(new GetUserInfoDto(Collections.singletonList(uid)))
                .map(RxUtils.bodyConverter())
                .map(userInfoBeans -> {
                    UserInfoBean g = userInfoBeans.get(0);
                    sTempUserInfo.put(g.getUid(), g);
                    return g;
                });
    }

    public static Observable<List<UserInfoBean>> getUserInfo(List<Long> uid) {
        final List<UserInfoBean> temped = new ArrayList<>();
        List<Long> filtered = new ArrayList<>();
        for (Long id : uid) {
            if (!sTempUserInfo.containsKey(id)) {
                filtered.add(id);
            } else {
                temped.add(sTempUserInfo.get(id));
            }
        }
        if (filtered.isEmpty()) {
            return Observable.just(temped);
        }
        Observable<Response<List<UserInfoBean>>> s = UserApi.API.getUserInfo(new GetUserInfoDto(filtered));
        return s.map(RxUtils.bodyConverter()).map(r -> {
            for (UserInfoBean u : r) {
                sTempUserInfo.put(u.getUid(), u);
            }
            temped.addAll(r);
            return r;
        });
    }

    public static Observable<CreateGroupBean> createGroup(String name) {
        return GroupApi.API.createGroup(new CreateGroupDto(name))
                .map(RxUtils.bodyConverter());
    }

    public static IMAccount getAccount() {
        return getInstance().account;
    }

    public static DataStorage getDataStorage() {
        return getInstance().dataStorage;
    }

    public void setDataStorage(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    public void setDevice(int device) {

    }
}
