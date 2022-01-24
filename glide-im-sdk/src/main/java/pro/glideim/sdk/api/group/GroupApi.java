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

    @POST("group/members")
    Observable<Response<List<GroupMemberBean>>> getGroupMember(@Body GetGroupMemberDto d);

    @POST("group/members/invite")
    Observable<Response<Object>> inviteMember(@Body AddGroupMemberDto d);

    @POST("group/members/remove")
    Observable<Response<Object>> removeMember(@Body RemoveMemberDto d);

    @POST("group/create")
    Observable<Response<CreateGroupBean>> createGroup(@Body CreateGroupDto d);

    @POST("group/join")
    Observable<Response<Object>> joinGroup(@Body JoinGroupDto d);
}
