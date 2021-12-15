package pro.glideim.sdk.api.msg;

import java.util.List;

import io.reactivex.Observable;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.http.RetrofitManager;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MsgApi {

    MsgApi API = RetrofitManager.create(MsgApi.class);

    @POST("msg/chat/recent")
    Observable<Response<List<MessageBean>>> getRecentChatMessage();

    @POST("msg/chat/user")
    Observable<Response<List<UserMsgBean>>> getChatMessageByUsers(@Body GetUserMsgDto d);

    @POST("msg/chat/history")
    Observable<Response<List<MessageBean>>> getChatMessageHistory(@Body GetChatHistoryDto d);

    @POST("msg/group/recent")
    Observable<Response<List<GroupMessageBean>>> getRecentGroupMessage(@Body GetGroupMsgHistoryDto d);

    @POST("msg/group/history")
    Observable<Response<List<GroupMessageBean>>> getGroupMessageHistory(@Body GetGroupMsgHistoryDto d);

    @POST("msg/group/state")
    Observable<Response<GroupMessageStateBean>> getGroupMessageState(@Body GetGroupMessageStateDto d);

    @POST("msg/chat/offline")
    Observable<Response<List<MessageBean>>> getOfflineMsg();

    @POST("msg/chat/offline/ack")
    Observable<Response<Object>> ackOfflineMsg(@Body AckOfflineMsgDto d);

    @POST("session/recent")
    Observable<Response<List<SessionBean>>> getRecentSession();

    @POST("session/get")
    Observable<Response<SessionBean>> getSession(@Body GetSessionDto d);

    @POST("session/update")
    Observable<Response<Object>> updateSession(@Body GetSessionDto d);
}
