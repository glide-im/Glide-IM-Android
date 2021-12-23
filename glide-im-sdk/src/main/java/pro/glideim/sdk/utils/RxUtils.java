package pro.glideim.sdk.utils;

import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pro.glideim.sdk.api.Response;

public class RxUtils {
    public static <T> ObservableTransformer<T, T> silentScheduler() {
        return upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread());
    }

    public static <T> Function<Response<T>, T> bodyConverter() {
        return r -> {
            if (!r.success()) {
                throw new Exception(r.getCode() + "," + r.getMsg());
            }
            return r.getData();
        };
    }
}
