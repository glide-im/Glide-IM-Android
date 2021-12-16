package pro.glideim

import android.app.Application
import com.dengzii.adapter.EmptyViewHolder
import com.dengzii.adapter.SuperAdapter
import pro.glideim.sdk.GlideIM

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        UserPerf.init(this)
        GlideIM.init("ws://192.168.1.123:8080/ws", "http://192.168.1.123:8081/api/")
        GlideIM.getInstance().setDataStorage(UserPerf.getInstance())
        GlideIM.getInstance().setDevice(1)
        GlideIM.getInstance()

        SuperAdapter.addDefaultViewHolderForType(
            SuperAdapter.Empty::class.java,
            EmptyViewHolder::class.java
        )
    }
}