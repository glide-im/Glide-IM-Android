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
import pro.glideim.sdk.push.NewContactsMessage
import pro.glideim.ui.Events
import pro.glideim.ui.LoginActivity
import pro.glideim.ui.SplashActivity
import pro.glideim.ui.chat.ChatActivity
import pro.glideim.utils.io2main
import pro.glideim.utils.request

class MessageListener private constructor(private val context: Application) : IMMessageListener {

    companion object {

        private lateinit var instance: MessageListener
        private val notifyChannelMsg = NotificationUtils.ChannelConfig(
            "pro.glideim.message",
            "GlideIM-Message",
            NotificationUtils.IMPORTANCE_HIGH
        )
        private val notifyChannelNotify = NotificationUtils.ChannelConfig(
            "pro.glideim.notify",
            "GlideIM-Notify",
            NotificationUtils.IMPORTANCE_HIGH
        )

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
        val systemService =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (message.type == Constants.MESSAGE_TYPE_RECALL) {
            // hide notification
            return
        }

        val s = ChatActivity.getCurrentSession()
        if (s?.to == message.to) {
            return
        }
        val intent = ChatActivity.getIntent(context, message.targetId, message.targetType)
        val onClick =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val se =
            GlideIM.getAccount().imSessionList.getOrCreate(message.targetType, message.targetId)
        val to = se.to
        var unread = se.unread
        if (unread == 0) {
            unread = 1
        }

        val delIntent = Intent("pro.glideim.notify.message.delete")
        delIntent.putExtra("session", to)
        delIntent.putExtra("type", se.type)
        val onDel =
            PendingIntent.getActivity(context, 0, delIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = NotificationUtils.getNotification(notifyChannelMsg) {
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
        systemService.notify(to.hashCode(), notification)
    }

    override fun onNewContact(c: NewContactsMessage) {

        val infoLoaded = { title: String ->
            val notification = NotificationUtils.getNotification(notifyChannelMsg) {
                val content = when (c.type) {
                    Constants.SESSION_TYPE_GROUP -> "用户 ${c.from} 邀请你到群聊 $title"
                    Constants.SESSION_TYPE_USER -> "用户 ${c.from} 已和你成为好友"
                    else -> "-"
                }
                it.setContentText(content)
                it.setContentTitle("新联系人")
                it.setSmallIcon(R.mipmap.ic_launcher)
                it.setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.mipmap.ic_launcher
                    )
                )
                val intent = ChatActivity.getIntent(context, c.id, c.type)
                val onClick =
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                it.setContentIntent(onClick)
                it.setOnlyAlertOnce(true)
                it.setAutoCancel(true)
            }
            val systemService =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemService.notify(c.id.hashCode(), notification)
        }

        when (c.type) {
            Constants.SESSION_TYPE_GROUP -> {
                GlideIM.getGroupInfo(c.id)
                    .io2main()
                    .request {
                        onSuccess {
                            infoLoaded(it.name)
                        }
                    }
            }
            Constants.SESSION_TYPE_USER -> {
                GlideIM.getUserInfo(c.id)
                    .io2main()
                    .request {
                        onSuccess {
                            infoLoaded(it.nickname)
                        }
                    }
            }
        }
        Events.updateContacts()
    }

    override fun onKickOut() {
        ActivityUtils.getTopActivity().let { activity ->
            if (activity is SplashActivity) {
                return
            }
            val apply = MaterialAlertDialogBuilder(activity).apply {
                setTitle("被迫下线")
                setMessage("你的账号在另一台设备上登录")
                setCancelable(false)
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
                setCancelable(false)
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