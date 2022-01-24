package pro.glideim

import android.app.Application
import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pro.glideim.db.GlideIMDatabase
import pro.glideim.db.Message
import pro.glideim.db.Session
import pro.glideim.db.UserInfo
import pro.glideim.sdk.*
import pro.glideim.sdk.api.group.GroupInfoBean
import pro.glideim.sdk.api.user.UserInfoBean

class IMDataStorage : DataStorage {

    private var token = ""
    private var uid = 0L
    private val d = DefaultDataStoreImpl()
    private val mUserInfo = mutableMapOf<Long, UserInfoBean>()

    companion object {
        private val instance = IMDataStorage()
        private lateinit var application: Application

        fun init(app: Application) {
            val sp = app.getSharedPreferences("u", Context.MODE_PRIVATE)
            instance.token = sp.getString("token", "") ?: ""
            instance.uid = sp.getLong("d_uid", 0)
            application = app
        }

        fun logout() {
            instance.storeToken(instance.uid, "")
        }

        fun getInstance(): IMDataStorage {
            return instance
        }
    }

    override fun getDefaultAccountUid(): Long {
        return uid
    }

    override fun storeToken(uid: Long, token: String?) {
        this.token = token ?: ""
        val sp = application.getSharedPreferences("u", Context.MODE_PRIVATE)
        val edit = sp.edit()
        edit.putString("token", token)
        edit.putLong("d_uid", uid)
        edit.apply()
    }

    override fun loadToken(uid: Long): String {
        return token
    }

    override fun storeTempUserInfo(userInfoBean: UserInfoBean) {
        mUserInfo[userInfoBean.uid] = userInfoBean
        GlobalScope.launch {
            GlideIMDatabase.getDb(application).apply {
                val userInfoDao = userInfoDao()
                val s = UserInfo.fromUserInfoBean(userInfoBean)
                val exist = userInfoDao.exist(userInfoBean.uid)
                if (exist != 0) {
                    userInfoDao.update(s)
                } else {
                    userInfoDao.add(s)
                }
                close()
            }
        }
    }

    override fun loadTempUserInfo(uid: Long): UserInfoBean? {
        val c = mUserInfo[uid]
        if (c != null) {
            return c
        }
        GlideIMDatabase.getDb(application).apply {
            val userInfoDao = userInfoDao()
            val ses = userInfoDao.get(uid)
            close()
            val expired = (System.currentTimeMillis() - (ses?.updateAt ?: 0)) / 1000 / 60 / 60 > 1
            if (expired) {
                return null
            }
            ses ?: return null
            mUserInfo[uid] = ses.toUserInfoBean()
            return ses.toUserInfoBean()
        }
    }

    override fun storeTempGroupInfo(groupInfoBean: GroupInfoBean?) {
        d.storeTempGroupInfo(groupInfoBean)
    }

    override fun loadTempGroupInfo(gid: Long): GroupInfoBean? {
        return d.loadTempGroupInfo(gid)
    }

    override fun storeSession(uid: Long, session: IMSession) {

        GlobalScope.launch {

            GlideIMDatabase.getDb(application).apply {
                val sessionDao = sessionDao()
                val exist = sessionDao.exist("$uid@${session.to}")
                val s = Session.fromIMSession(uid, session)
                if (exist == 1) {
                    sessionDao.update(s)
                } else {
                    sessionDao.add(s)
                }
                close()
            }

        }
    }

    override fun loadSessions(uid: Long): MutableList<IMSession> {
        GlideIMDatabase.getDb(application).apply {
            val sessionDao = sessionDao()
            val ses = sessionDao.get(uid)
            close()
            return ses.map { it.toImSession(GlideIM.getAccount()) }.toMutableList()
        }
    }

    override fun loadMessage(uid: Long, type: Int, to: Long): MutableList<IMMessage> {
        GlideIMDatabase.getDb(application).apply {
            val messageDao = messageDao()
            val ses = messageDao.get(uid, type, to)
            close()
            return ses.map { it.toIMMessage(GlideIM.getAccount()) }.toMutableList()
        }
    }

    override fun storeMessage(message: IMMessage) {
        GlobalScope.launch {
            GlideIMDatabase.getDb(application).apply {
                val messageDao = messageDao()
                val exist = messageDao.exist(message.mid)
                val s = Message.fromIMMessage(uid, message)
                if (exist == 1) {
                    messageDao.update(s)
                } else {
                    messageDao.add(s)
                }
                close()
            }
        }
    }
}