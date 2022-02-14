package pro.glideim.sdk.api.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pro.glideim.sdk.DefaultDataStoreImpl;
import pro.glideim.sdk.GlideIM;
import pro.glideim.sdk.TestResObserver;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.http.RetrofitManager;

class AppApiTest {


    @BeforeEach
    void tearDown() {
        RetrofitManager.init("http://localhost:8081/api/");
        GlideIM.init("http://localhost:8081/api/");
        GlideIM.getInstance().setDataStorage(new DefaultDataStoreImpl());
    }

    @Test
    void getReleaseInfo() {
        AppApi.API.getReleaseInfo()
                .subscribe(new TestResObserver<Response<ReleaseInfoBean>>() {
                    @Override
                    public void onNext(Response<ReleaseInfoBean> releaseInfoBeanResponse) {
                        System.out.println(RetrofitManager.toJson(releaseInfoBeanResponse.getData()));
                    }
                });
    }
}