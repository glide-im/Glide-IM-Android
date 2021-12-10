package pro.glideim.sdk;

import androidx.annotation.NonNull;

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

        Observable<List<User.Contacts>> result = UserApi.API.getContactsList()
                .map(bodyConverter())
                .flatMap((Function<List<ContactsBean>, ObservableSource<ContactsBean>>) Observable::fromIterable)
                .groupBy(ContactsBean::getType)
                .switchMap(new Function<GroupedObservable<Integer, ContactsBean>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(@NonNull GroupedObservable<Integer, ContactsBean> go) throws Exception {

                        return null;
                    }
                })
                .flatMap((Function<List<ContactsBean>, ObservableSource<List<User.Contacts>>>) contactsBeans -> {
//                    List<Long> uid = new ArrayList<>();
//                    List<Long> gid = new ArrayList<>();
//                    for (ContactsBean datum : contactsBeans) {
//                        switch (datum.getType()) {
//                            case 1:
//                                uid.add(datum.getId());
//                                break;
//                            case 2:
//                                gid.add(datum.getId());
//                                break;
//                        }
//                    }
//                    Observable<List<UserInfoBean>> s = getUserInfo(uid);
//                    Observable<List<GroupInfoBean>> g = getGroupInfo(uid);
//
//                    return s.map(userInfoBeans -> {
//                        List<User.Contacts> cs = new ArrayList<>();
//                        for (UserInfoBean infoBean : userInfoBeans) {
//                            User.Contacts c = new User.Contacts();
//                            c.avatar = infoBean.getAvatar();
//                            c.id = infoBean.getUid();
//                            c.title = infoBean.getNickname();
//                            c.type = 1;
//                            cs.add(new User.Contacts());
//                        }
//                        return cs;
//                    });
                });


        return result;
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
