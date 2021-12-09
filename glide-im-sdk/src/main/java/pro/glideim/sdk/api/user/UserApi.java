package pro.glideim.sdk.api.user;

import io.reactivex.Observable;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.http.RetrofitManager;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;

public interface UserApi {

    UserApi API = RetrofitManager.create(UserApi.class);

    @POST("contacts/list")
    Observable<Response<List<ContactsBean>>> getContactsList();

    @POST("contacts/add")
    Observable<Response<Object>> addContacts(@Body ContactsUidDto d);

    @POST("contacts/del")
    Observable<Response<Object>> delContacts(@Body ContactsUidDto d);

    @POST("contacts/approval")
    Observable<Response<Object>> contactsApproval(@Body ApprovalContactsDto d);

    @POST("user/info")
    Observable<Response<List<UserInfoBean>>> getUserInfo(@Body GetUserInfoDto d);

    @POST("user/profile")
    Observable<Response<ProfileBean>> myProfile();

    @POST("user/profile/update")
    Observable<Response<Object>> updateMyProfile();
}
