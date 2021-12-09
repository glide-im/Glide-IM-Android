package pro.glideim.sdk.api.msg;

import io.reactivex.Observable;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.http.RetrofitManager;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;

public interface MsgApi {

    MsgApi API = RetrofitManager.create(MsgApi.class);

    @POST("msg/recent")
    Observable<Response<List<RecentMsgBean>>> recentMsg(@Body GetRecentMsgDto d);

    @POST("msg/history")
    Observable<Response<List<MessageBean>>> history(@Body GetChatHistoryDto d);

    @POST("msg/offline")
    Observable<Response<List<MessageBean>>> getOfflineMsg();

    @POST("msg/offline/ack")
    Observable<Response<Object>> ackOfflineMsg(@Body AckOfflineMsgDto d);

    @POST("session/recent")
    Observable<Response<List<SessionBean>>> getRecentSession();

    @POST("session/get")
    Observable<Response<SessionBean>> getSession(@Body GetSessionDto d);

    @POST("session/update")
    Observable<Response<Object>> updateSession(@Body GetSessionDto d);
}
