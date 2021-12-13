package pro.glideim.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.api.group.GetGroupInfoDto;
import pro.glideim.sdk.api.group.GroupApi;
import pro.glideim.sdk.api.group.GroupInfoBean;
import pro.glideim.sdk.api.user.ContactsBean;
import pro.glideim.sdk.api.user.GetUserInfoDto;
import pro.glideim.sdk.api.user.UserApi;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.entity.User;
import pro.glideim.sdk.http.RetrofitManager;

public class GlideIM {

    private static final IMClient sIM = new IMClient();

    private static final Map<Long, UserInfoBean> sTempUserInfo = new HashMap<>();
    private static final Map<Long, GroupInfoBean> sTempGroupInfo = new HashMap<>();

    public static Long getMyUID() {
        return 1L;
    }

    public static void init(String wsUrl, String baseUrlApi) {
        RetrofitManager.init(baseUrlApi);
//        sIM.connect(wsUrl);
    }


    public static Observable<List<User.Contacts>> getContacts() {
        return UserApi.API.getContactsList()
                .map(bodyConverter())
                .flatMap((Function<List<ContactsBean>, ObservableSource<ContactsBean>>) Observable::fromIterable)
                .groupBy(ContactsBean::getType, ContactsBean::getId)
                .flatMap((Function<GroupedObservable<Integer, Long>, ObservableSource<List<User.Contacts>>>) group -> {
                    if (group.getKey() == null) {
                        return Observable.empty();
                    }
                    Observable<List<Long>> idsOb = group.toList().toObservable();
                    switch (group.getKey()) {
                        case 1:
                            return idsOb
                                    .flatMap((Function<List<Long>, ObservableSource<List<UserInfoBean>>>) GlideIM::getUserInfo)
                                    .map(userInfoBeans -> {
                                        List<User.Contacts> r = new ArrayList<>();
                                        for (UserInfoBean userInfoBean : userInfoBeans) {
                                            User.Contacts c = new User.Contacts();
                                            c.title = userInfoBean.getNickname();
                                            c.avatar = userInfoBean.getAvatar();
                                            c.id = userInfoBean.getUid();
                                            c.type = 1;
                                            r.add(c);
                                        }
                                        return r;
                                    });
                        case 2:
                            return idsOb
                                    .flatMap((Function<List<Long>, ObservableSource<List<GroupInfoBean>>>) GlideIM::getGroupInfo)
                                    .map(groupInfoBeans -> {
                                        List<User.Contacts> r = new ArrayList<>();
                                        for (GroupInfoBean userInfoBean : groupInfoBeans) {
                                            User.Contacts c = new User.Contacts();
                                            c.title = userInfoBean.getName();
                                            c.avatar = userInfoBean.getAvatar();
                                            c.id = userInfoBean.getGid();
                                            c.type = 2;
                                            r.add(c);
                                        }
                                        return r;
                                    });
                        default:
                            return Observable.empty();
                    }

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

    private static <T> Function<Response<T>, T> bodyConverter() {
        return r -> {
            if (!r.success()) {
                throw new Exception(r.getCode() + "," + r.getMsg());
            }
            return r.getData();
        };
    }
}
