package pro.glideim.sdk;

import androidx.annotation.NonNull;

import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import pro.glideim.sdk.http.RetrofitManager;

public class TestObserver<T> implements Observer<T> , SingleObserver<T> {
    @Override
    public void onSubscribe(@NotNull Disposable d) {
        System.out.println("TestObserver.onSubscribe");
    }

    @Override
    public void onSuccess(@NonNull T t) {
        System.out.println("TestObserver.onSuccess");
        System.out.println(RetrofitManager.toJson(t));
    }

    @Override
    public void onNext(@NotNull T t) {
        System.out.println("TestObserver.onNext");
        System.out.println(RetrofitManager.toJson(t));
    }

    @Override
    public void onError(@NotNull Throwable e) {
        System.out.println("TestObserver.onError");
        e.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println("TestObserver.onComplete");
    }
}
