package pro.glideim.sdk;

import pro.glideim.sdk.http.RetrofitManager;

public class GlideIM {

    public static void init(String hostHost, String socketPort, String baseUrlApi) {
        RetrofitManager.init(baseUrlApi);
    }
}
