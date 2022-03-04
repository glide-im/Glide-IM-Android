package pro.glideim.base

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.IMAccount
import pro.glideim.sdk.im.ConnStateListener
import pro.glideim.sdk.ws.WsClient
import pro.glideim.ui.LoginActivity
import pro.glideim.utils.RequestStateCallback

abstract class BaseActivity : AppCompatActivity(), RequestStateCallback, ConnStateListener {

    private val TAG = BaseActivity::class.java.simpleName

    abstract val layoutResId: Int

    private var inited = false

    protected var needAuth = true

    open val account: IMAccount? get() = GlideIM.getAccount()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
    }

    override fun onStart() {
        super.onStart()
        if (!inited) {
            initView()
            inited = true
        }
        account?.imClient?.apply {
            onStateChange(webSocketClient.state, "")
        }
        account?.imClient?.addConnStateListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (needAuth && (account == null || account?.uid == 0L)) {
            LoginActivity.start(this)
            finish()
        }
    }

    abstract fun initView()

    fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestStart() {

    }

    override fun onRequestFinish() {

    }

    override fun onRequestError(t: Throwable) {
        toast(t.message ?: "error")
    }

    override fun onStop() {
        super.onStop()
        account?.imClient?.removeConnStateListener(this)
    }

    open fun updateConnState(state: String) {

    }

    override fun onStateChange(state: Int, msg: String?) {
        Log.d(TAG, "onStateChange: $state")
        val s = when (state) {
            WsClient.STATE_CLOSED -> {
                "disconnected"
            }
            WsClient.STATE_CONNECTING -> "connecting"
            WsClient.STATE_OPENED -> ""
            else -> ""
        }
        runOnUiThread { updateConnState(s) }
    }
}