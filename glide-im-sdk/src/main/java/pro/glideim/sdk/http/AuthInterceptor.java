package pro.glideim.sdk.http;

import java.io.IOException;
import java.net.ConnectException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import pro.glideim.sdk.GlideIM;

public class AuthInterceptor implements Interceptor {

    private static final AuthInterceptor instance = new AuthInterceptor();

    public static AuthInterceptor create() {
        return instance;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder request = chain.request().newBuilder();

        String jwtToken = GlideIM.getDataStorage().loadToken(GlideIM.getAccount().uid);
        request.addHeader("Authorization", "Bearer " + jwtToken);
        return chain.proceed(request.build());
    }
}
