package pro.glideim.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.api.user.ContactsBean;
import pro.glideim.sdk.api.user.GetUserInfoDto;
import pro.glideim.sdk.api.user.UserApi;
import pro.glideim.sdk.api.user.UserInfoBean;
import pro.glideim.sdk.entity.User;
import pro.glideim.sdk.http.RetrofitManager;

public class GlideIM {

    private static final IMClient sIM = new IMClient();

    private static final Map<Long, UserInfoBean> sTempUserInfo = new HashMap<>();

    public static Long getMyUID() {
        return 1L;
    }

    public static void init(String wsUrl, String baseUrlApi) {
        RetrofitManager.init(baseUrlApi);
//        sIM.connect(wsUrl);
    }


    public static Observable<Response<List<User.Contacts>>> getContacts() {

        Observable<Response<List<User.Contacts>>> result = UserApi.API.getContactsList()
                .flatMap((Function<Response<List<ContactsBean>>, ObservableSource<Response<List<User.Contacts>>>>) response -> {
                    if (response.getCode() != 100) {
                        throw new Exception(response.getCode() + "," + response.getMsg());
                    }
                    List<Long> uid = new ArrayList<>();
                    List<Long> gid = new ArrayList<>();
                    for (ContactsBean datum : response.getData()) {
                        switch (datum.getType()) {
                            case 1:
                                uid.add(datum.getId());
                                break;
                            case 2:
                                break;
                        }
                    }
                    Observable<Response<List<UserInfoBean>>> s = getUserInfo(uid);
                    return s.map(r -> {
                        Response<List<User.Contacts>> t = new Response<>();
                        t.setCode(r.getCode());
                        t.setMsg(r.getMsg());
                        for (int i = 0; i < r.getData().size(); i++) {

                        }
                        return t;
                    });
                });


        return Observable.empty();
    }

    public static Observable<Response<List<UserInfoBean>>> getUserInfo(List<Long> uid) {
        List<UserInfoBean> temped = new ArrayList<>();
        List<Long> filtered = new ArrayList<>();
        for (Long id : uid) {
            if (!sTempUserInfo.containsKey(id)) {
                filtered.add(id);
            } else {
                temped.add(sTempUserInfo.get(id));
            }
        }
        Observable<Response<List<UserInfoBean>>> s = UserApi.API.getUserInfo(new GetUserInfoDto(filtered));
        return s.map(r -> {
            temped.addAll(r.getData());
            r.setData(temped);
            return r;
        });
    }
}
