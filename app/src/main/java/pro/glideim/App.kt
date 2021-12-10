package pro.glideim

import android.app.Application
import com.dengzii.adapter.EmptyViewHolder
import com.dengzii.adapter.SuperAdapter
import pro.glideim.sdk.GlideIM

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        GlideIM.init("ws://192.168.1.123:8080/ws", "http://192.168.1.123:8081/api/")
        SuperAdapter.addDefaultViewHolderForType(SuperAdapter.Empty::class.java, EmptyViewHolder::class.java)
    }
}