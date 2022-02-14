package pro.glideim.sdk.api.app;

import io.reactivex.Observable;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.http.RetrofitManager;
import retrofit2.http.GET;

public interface AppApi {
    AppApi API = RetrofitManager.create(AppApi.class);

    @GET("app/release")
    Observable<Response<ReleaseInfoBean>> getReleaseInfo();
}
