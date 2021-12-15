package pro.glideim.sdk.utils;

import io.reactivex.ObservableTransformer;
import io.reactivex.schedulers.Schedulers;

public class RxUtils {
    public static <T> ObservableTransformer<T, T> silentScheduler() {
        return upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread());
    }
}
