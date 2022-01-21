package pro.glideim

import android.app.Application
import com.dengzii.adapter.SuperAdapter
import pro.glideim.sdk.GlideIM

class App : Application() {

    companion object {
        private const val TAG = "App"
    }

    override fun onCreate() {
        super.onCreate()

        IMDataStorage.init(this)
        MessageListener.init(this)

//        SLogger.setLogger(object : Logger {
//            override fun d(tag: String, log: String) {
//                Log.d(tag, log)
//            }
//
//            override fun e(tag: String, t: Throwable) {
//                Log.e(tag, "e: ", t)
//            }
//        })
        GlideIM.init(BuildConfig.BASE_URL)
        GlideIM.getInstance().dataStorage = IMDataStorage.getInstance()
        GlideIM.getInstance().setDevice(1)

        SuperAdapter.addDefaultViewHolderForType(
            SuperAdapter.Empty::class.java,
            pro.glideim.viewholder.EmptyViewHolder::class.java
        )
    }
}