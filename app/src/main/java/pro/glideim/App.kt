package pro.glideim

import android.app.Application
import android.util.Log
import com.dengzii.adapter.EmptyViewHolder
import com.dengzii.adapter.SuperAdapter
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.Logger
import pro.glideim.sdk.utils.SLogger

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
        GlideIM.init("http://192.168.1.123:8081/api/")
        GlideIM.getInstance().dataStorage = IMDataStorage.getInstance()
        GlideIM.getInstance().setDevice(1)

        SuperAdapter.addDefaultViewHolderForType(
            SuperAdapter.Empty::class.java,
            EmptyViewHolder::class.java
        )
    }
}