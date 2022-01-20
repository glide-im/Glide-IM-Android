package pro.glideim

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.NotificationUtils
import com.blankj.utilcode.util.ToastUtils
import com.dengzii.ktx.android.showWithLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.Constants
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.IMMessage
import pro.glideim.sdk.IMMessageListener
import pro.glideim.ui.Events
import pro.glideim.ui.LoginActivity
import pro.glideim.ui.chat.ChatActivity

class MessageListener private constructor(private val context: Application) : IMMessageListener {

    companion object {

        private lateinit var instance: MessageListener

        fun init(context: Application) {
            instance = MessageListener(context)
        }

        fun getInstance(): MessageListener {
            return instance
        }
    }

    override fun onNotify(msg: String) {
        ToastUtils.showShort(msg)
    }

    override fun onNewMessage(message: IMMessage) {

        val s = ChatActivity.getCurrentSession()
        if (s?.to == message.to) {
            return
        }

        val c = NotificationUtils.ChannelConfig(
            "pro.glideim.message",
            "GlideIM-Message",
            NotificationUtils.IMPORTANCE_HIGH
        )

        val intent = ChatActivity.getIntent(context, message.targetId, message.targetType)
        val onClick =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)


        val to = message.session.to
        var unread = message.session.unread
        if (unread == 0) {
            unread = 1
        }

        val delIntent = Intent("pro.glideim.notify.message.delete")
        delIntent.putExtra("session", to)
        delIntent.putExtra("type", message.session.type)
        val onDel =
            PendingIntent.getActivity(context, 0, delIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = NotificationUtils.getNotification(c) {
            it.setContentText(message.content)
            it.setContentTitle("来自${message.title}的${unread}条未读消息")
            it.setSmallIcon(R.mipmap.ic_launcher)
            it.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            it.setContentIntent(onClick)
            it.setDeleteIntent(onDel)
            it.setNumber(unread)
            it.setOnlyAlertOnce(true)
            it.setAutoCancel(true)
        }
        val systemService =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        systemService.notify(to.hashCode(), notification)
    }

    override fun onNewContact() {
        Events.updateContacts()
    }

    override fun onKickOut() {
        ActivityUtils.getTopActivity().let { activity ->
            val apply = MaterialAlertDialogBuilder(activity).apply {
                setTitle("被迫下线")
                setMessage("你的账号在另一台设备上登录")
                setPositiveButton("确定") { d, _ ->
                    d.dismiss()
                    LoginActivity.start(activity)
                }
            }
            activity.runOnUiThread {
                apply.create().showWithLifecycle(activity as BaseActivity)
            }
        }
    }

    override fun onTokenInvalid() {
        ActivityUtils.getTopActivity()?.let { activity ->
            val block: MaterialAlertDialogBuilder.() -> Unit = {
                setTitle("登录过期")
                setMessage("登录身份信息已过期, 请重新登录")
                setPositiveButton("确定") { d, _ ->
                    d.dismiss()
                    LoginActivity.start(activity)
                }
            }
            activity.runOnUiThread {
                MaterialAlertDialogBuilder(activity).apply(block).create().show()
            }
        }
    }
}