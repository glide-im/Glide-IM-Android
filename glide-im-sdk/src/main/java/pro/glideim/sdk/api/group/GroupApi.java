package pro.glideim.sdk.api.group;

import java.util.List;

import io.reactivex.Observable;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.http.RetrofitManager;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GroupApi {

    GroupApi API = RetrofitManager.create(GroupApi.class);

    @POST("group/info")
    Observable<Response<List<GroupInfoBean>>> getGroupInfo(@Body GetGroupInfoDto d);

    @POST("group/create")
    Observable<Response<CreateGroupBean>> createGroup(@Body CreateGroupDto d);

    @POST("group/join")
    Observable<Response<GroupInfoBean>> joinGroup();
}
