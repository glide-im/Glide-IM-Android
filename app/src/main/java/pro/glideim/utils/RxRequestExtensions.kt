package pro.glideim.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import pro.glideim.sdk.api.Response


interface RequestStateCallback {
    fun onRequestStart()
    fun onRequestFinish()
    fun onRequestError(t: Throwable)
}

fun <T> Observable<T>.io2main(): Observable<T> {
    return compose { observable ->
        observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> Single<T>.io2main(): Single<T> {
    return compose { observable ->
        observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> Observable<Response<T>>.resolve(): Observable<T> {
    return compose { observable ->
        observable
            .io2main()
            .flatMap {
                if (it.code == 100) {
                    if (it.data != null) {
                        Observable.just(it.data)
                    } else {
                        Observable.empty()
                    }
                } else {
                    throw Exception("${it.code}, ${it.msg}")
                }
            }
    }
}

fun <T> Observable<Response<T>>.convert(): Observable<T> {
    return resolve()
}

fun <T> Observable<Response<T>>.subscribeWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    observer: ObserverBuilder<T>.() -> Unit
) {
    if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        throw IllegalStateException("lifecycle owner is destroyed.")
    }
    val proxy = ProxyObserver<T>()
    observer.invoke(ObserverBuilder(proxy))
    this.convert()
        .subscribe(proxy)
    lifecycleOwner.lifecycle
        .addObserver(proxy)
}

fun <T> Observable<Response<T>>.request(observer: ObserverBuilder<T>.() -> Unit) {

    val proxy = ProxyObserver<T>()
    observer.invoke(ObserverBuilder(proxy))
    this.convert()
        .subscribe(proxy)
}

fun <T> Single<T>.request2(activity: RequestStateCallback, callback: (r: T?) -> Unit) {

    val proxy = ProxyObserver<T>()
    val b: ObserverBuilder<T>.() -> Unit = {
        onStart {
            activity.onRequestStart()
        }
        onFinish {
            activity.onRequestFinish()
        }
        onError {
            activity.onRequestError(it)
        }
        onSuccess {
            callback(it)
        }
        onSuccessNull {
            callback(null)
        }
    }
    b.invoke(ObserverBuilder(proxy))
    this.subscribe(proxy)
}

fun <T> Observable<T>.request2(activity: RequestStateCallback, callback: (r: T?) -> Unit) {

    val proxy = ProxyObserver<T>()
    val b: ObserverBuilder<T>.() -> Unit = {
        onStart {
            activity.onRequestStart()
        }
        onFinish {
            activity.onRequestFinish()
        }
        onError {
            activity.onRequestError(it)
        }
        onSuccess {
            callback(it)
        }
        onSuccessNull {
            callback(null)
        }
    }
    b.invoke(ObserverBuilder(proxy))
    this.subscribe(proxy)
}


fun <T> Observable<Response<T>>.request(activity: RequestStateCallback, callback: (r: T?) -> Unit) {

    val proxy = ProxyObserver<T>()
    val b: ObserverBuilder<T>.() -> Unit = {
        onStart {
            activity.onRequestStart()
        }
        onFinish {
            activity.onRequestFinish()
        }
        onError {
            activity.onRequestError(it)
        }
        onSuccess {
            callback(it)
        }
        onSuccessNull {
            callback(null)
        }
    }
    b.invoke(ObserverBuilder(proxy))
    this.convert()
        .subscribe(proxy)
}

open class ProxyObserver<T> : Observer<T>, LifecycleEventObserver, SingleObserver<T> {

    internal var start: (Disposable) -> Unit = {}
    internal var success: (T) -> Unit = {}
    internal var success2: () -> Unit = {}
    internal var error: (Throwable) -> Unit = { }
    internal var finish: () -> Unit = {}

    private lateinit var disposable: Disposable

    private var nexted = 0
    private var errored = false

    override fun onSubscribe(d: Disposable) {
        disposable = d
        start.invoke(d)
    }

    override fun onNext(t: T) {
        nexted++
        success.invoke(t)
    }

    override fun onError(e: Throwable) {
        errored = true
        error.invoke(e)
        finish.invoke()
    }

    override fun onComplete() {
        if (!errored && nexted == 0) {
            success2()
        }
        finish.invoke()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (!this::disposable.isInitialized) {
            return
        }
        if (event == Lifecycle.Event.ON_STOP) {
            if (!disposable.isDisposed) {
                disposable.dispose()
                source.lifecycle.removeObserver(this)
            }
        }
    }

    override fun onSuccess(t: T) {
        nexted++
        success.invoke(t)
    }
}

class ObserverBuilder<T>(private val proxyObserver: ProxyObserver<T>) {

    fun onStart(action: (Disposable) -> Unit) {
        proxyObserver.start = action
    }

    fun onSuccess(action: (T) -> Unit) {
        proxyObserver.success = action
    }

    fun onSuccessNull(action: () -> Unit) {
        proxyObserver.success2 = action
    }

    fun onError(action: (Throwable) -> Unit) {
        proxyObserver.error = action
    }

    fun onFinish(action: () -> Unit) {
        proxyObserver.finish = action
    }
}
