package pro.glideim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MessageNotifyActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        context ?: return
        val to = intent.getLongExtra("session", 0)
        val type = intent.getIntExtra("type", 0)
//        GlideIM.getAccount().imSessionList.getSession(type, to).clearUnread()

    }
}