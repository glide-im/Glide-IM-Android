package pro.glideim.sdk;

import androidx.annotation.NonNull;

import org.json.JSONStringer;

import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import pro.glideim.sdk.utils.SLogger;

public class SilentObserver<T> implements Observer<T>, SingleObserver<T> {

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onSuccess(@NonNull T t) {
        onNext(t);
    }

    @Override
    public void onNext(@NonNull T t) {

    }

    @Override
    public void onError(@NonNull Throwable e) {
        StackTraceElement stackTraceElement = e.getStackTrace()[0];
        SLogger.d("SilentObserver", e.getMessage() + " : " + stackTraceElement.toString());
    }

    @Override
    public void onComplete() {

    }
}
