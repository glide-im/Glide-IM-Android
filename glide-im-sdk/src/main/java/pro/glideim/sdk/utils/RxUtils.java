package pro.glideim.sdk.utils;

import io.reactivex.ObservableTransformer;
import io.reactivex.SingleTransformer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pro.glideim.sdk.GlideException;
import pro.glideim.sdk.api.Response;
import pro.glideim.sdk.messages.CommMessage;

public class RxUtils {
    public static <T> ObservableTransformer<T, T> silentScheduler() {
        return upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread());
    }

    public static <T> Function<Response<T>, T> bodyConverter() {
        return r -> {
            if (!r.success()) {
                throw new GlideException(r.getCode() + "," + r.getMsg());
            }
            return r.getData();
        };
    }

    public static <T> SingleTransformer<T, T> silentSchedulerSingle() {
        return upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread());
    }

    public static <T> Function<CommMessage<T>, T> bodyConverterForWsMsg() {
        return r -> {
            if (!r.success()) {
                throw new GlideException(r.getData().toString());
            }
            return r.getData();
        };
    }
}
