package pro.glideim.sdk.http;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import pro.glideim.sdk.GlideIM;

public class AuthInterceptor implements Interceptor {

    private static final AuthInterceptor instance = new AuthInterceptor();

    public static AuthInterceptor create(){
        return instance;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder request = chain.request().newBuilder();

        String jwtToken = GlideIM.getInstance().getDataStorage().loadToken();
        request.addHeader("Authorization", "Bearer "+jwtToken);
        return chain.proceed(request.build());
    }
}
