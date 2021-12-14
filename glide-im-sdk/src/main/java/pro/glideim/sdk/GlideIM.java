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
import pro.glideim.sdk.api.auth.LoginDto;
import pro.glideim.sdk.api.group.GetGroupInfoDto;
import pro.glideim.sdk.api.group.GroupApi;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.msg.GetGroupMessageStateDto;
import pro.glideim.sdk.api.msg.GroupMessageStateBean;
import pro.glideim.sdk.api.msg.MessageBean;
import pro.glideim.sdk.api.msg.MsgApi;
import pro.glideim.sdk.api.msg.SessionBean;
import pro.glideim.sdk.api.user.ContactsBean;
import pro.glideim.sdk.api.user.GetUserInfoDto;
import pro.glideim.sdk.api.user.UserApi;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.entity.IMContacts;
import pro.glideim.sdk.entity.IMSession;
import pro.glideim.sdk.entity.IdTag;
import pro.glideim.sdk.http.RetrofitManager;

public class GlideIM {

    private static final IMClient sIM = new IMClient();

    private static final Map<Long, UserInfoBean> sTempUserInfo = new HashMap<>();
    private static final Map<Long, GroupInfoBean> sTempGroupInfo = new HashMap<>();

    private static final Map<String, SessionBean> sTempSession = new HashMap<>();

    private static final UserInfo sUserInfo = new UserInfo();

    public static Long getMyUID() {
        return 1L;
    }

    public static void init(String wsUrl, String baseUrlApi) {
        RetrofitManager.init(baseUrlApi);
//        sIM.connect(wsUrl);
    }

    public static Observable<Boolean> login(String account, String password, int device) {
        return AuthApi.API.login(new LoginDto(account, password, device))
                .map(tokenBeanResponse -> {
                    if (tokenBeanResponse.success()) {
                        sUserInfo.token = tokenBeanResponse.getData().getToken();
                        sUserInfo.uid = tokenBeanResponse.getData().getUid();
                        return true;
                    }
                    return false;
                });
    }

    public static void subscribeSessionChange(SessionUpdateListener listener) {
        sUserInfo.sessionUpdateListener = listener;
    }

    public static void onContactsChange(ContactsChangeListener listener) {
        sUserInfo.contactsChangeListener = listener;
    }

    public static Observable<List<MessageBean>> getOfflineMessage() {
        return MsgApi.API.getOfflineMsg()
                .map(bodyConverter());
    }

    public static Observable<List<IMSession>> getSessionList() {

        List<Observable<Response<GroupMessageStateBean>>> st = new ArrayList<>();
        for (Long id : sUserInfo.getContactsGroup()) {
            st.add(MsgApi.API.getGroupMessageState(new GetGroupMessageStateDto(id)));
        }
        Observable<IMSession> groupSession = Observable.merge(st)
                .map(bodyConverter())
                .map(IMSession::fromGroupState)
                .flatMap((Function<IMSession, ObservableSource<IMSession>>) session ->
                        getGroupInfo(session.to).map(session::setGroupInfo)
                );

        Observable<IMSession> listObservable = MsgApi.API.getRecentSession()
                .map(bodyConverter())
                .map(sessionBeans -> {
                    List<IMSession> a = new ArrayList<>();
                    long myUID = getMyUID();
                    for (SessionBean sessionBean : sessionBeans) {
                        a.add(IMSession.fromSessionBean(myUID, sessionBean));
                    }
                    return a;
                })
                .flatMap((Function<List<IMSession>, ObservableSource<IMSession>>) imSessions -> {
                    List<Long> ids = new ArrayList<>();
                    Map<Long, IMSession> s = new HashMap<>();
                    for (IMSession imSession : imSessions) {
                        ids.add(imSession.to);
                        s.put(imSession.to, imSession);
                    }
                    return getUserInfo(ids)
                            .flatMap((Function<List<UserInfoBean>, ObservableSource<IMSession>>) userInfoBeans -> {
                                for (UserInfoBean userInfoBean : userInfoBeans) {
                                    IMSession imSession = s.get(userInfoBean.getUid());
                                    if (imSession == null) {
                                        continue;
                                    }
                                    imSession.setUserInfo(userInfoBean);
                                }
                                return Observable.fromIterable(s.values());
                            });
                });
        return Observable.merge(groupSession, listObservable)
                .toList()
                .toObservable();
    }

    public static Observable<List<IMContacts>> getContacts() {
        return UserApi.API.getContactsList()
                .map(bodyConverter())
                .map(contactsBeans -> {
                    List<IMContacts> c = new ArrayList<>();
                    for (ContactsBean contactsBean : contactsBeans) {
                        c.add(IMContacts.fromContactsBean(contactsBean));
                    }
                    sUserInfo.addContacts(contactsBeans);
                    return c;
                })
                .flatMap((Function<List<IMContacts>, ObservableSource<List<IMContacts>>>) contacts -> updateContactInfo());
    }

    public static Observable<GroupInfoBean> getGroupInfo(long gid) {
        if (sTempGroupInfo.containsKey(gid)) {
            return Observable.just(sTempGroupInfo.get(gid));
        }
        return GroupApi.API.getGroupInfo(new GetGroupInfoDto(Collections.singletonList(gid)))
                .map(bodyConverter())
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
        return s.map(bodyConverter()).map(r -> {
            for (GroupInfoBean u : r) {
                sTempGroupInfo.put(u.getGid(), u);
            }
            temped.addAll(r);
            return r;
        });
    }

    public static Observable<UserInfoBean> getUserInfo(long uid) {
        if (sTempUserInfo.containsKey(uid)) {
            return Observable.just(sTempUserInfo.get(uid));
        }
        return UserApi.API.getUserInfo(new GetUserInfoDto(Collections.singletonList(uid)))
                .map(bodyConverter())
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
        return s.map(bodyConverter()).map(r -> {
            for (UserInfoBean u : r) {
                sTempUserInfo.put(u.getUid(), u);
            }
            temped.addAll(r);
            return r;
        });
    }

    public static Observable<List<IMContacts>> updateContactInfo() {
        Iterable<IdTag> idList = sUserInfo.getContactsIdList();
        List<Observable<IMContacts>> obs = new ArrayList<>();

        Map<Integer, List<Long>> typeIdsMap = new HashMap<>();
        for (IdTag idTag : idList) {
            if (!typeIdsMap.containsKey(idTag.getType())) {
                typeIdsMap.put(idTag.getType(), new ArrayList<>());
            }
            //noinspection ConstantConditions
            typeIdsMap.get(idTag.getType()).add(idTag.getId());
        }

        typeIdsMap.forEach((type, ids) -> {
            Observable<IMContacts> ob = Observable.empty();
            switch (type) {
                case 1:
                    ob = getUserInfo(ids).map(sUserInfo::updateContacts).flatMap(Observable::fromIterable);
                    break;
                case 2:
                    ob = getGroupInfo(ids).map(sUserInfo::updateContactsGroup).flatMap(Observable::fromIterable);
                    break;
            }
            obs.add(ob);
        });

        return Observable.merge(obs).toList().toObservable();
    }

    private static <T> Function<Response<T>, T> bodyConverter() {
        return r -> {
            if (!r.success()) {
                throw new Exception(r.getCode() + "," + r.getMsg());
            }
            return r.getData();
        };
    }
}
