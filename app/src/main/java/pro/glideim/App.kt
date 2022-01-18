package pro.glideim

import android.app.Application
import com.dengzii.adapter.EmptyViewHolder
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
        GlideIM.init("http://192.168.1.123:8081/api/")
        GlideIM.getInstance().dataStorage = IMDataStorage.getInstance()
        GlideIM.getInstance().setDevice(1)

        SuperAdapter.addDefaultViewHolderForType(
            SuperAdapter.Empty::class.java,
            EmptyViewHolder::class.java
        )
    }
}