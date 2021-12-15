package pro.glideim.sdk;

import androidx.annotation.NonNull;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SilentObserver<T> implements Observer<T> {

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(@NonNull T t) {

    }

    @Override
    public void onError(@NonNull Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onComplete() {

    }
}