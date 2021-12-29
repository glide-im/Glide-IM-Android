package pro.glideim.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.im.ConnStateListener
import pro.glideim.sdk.ws.WsClient
import pro.glideim.utils.RequestStateCallback

abstract class BaseActivity : AppCompatActivity(), RequestStateCallback, ConnStateListener {
    abstract val layoutResId: Int

    private var inited = false

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
        onStateChange(GlideIM.getInstance().connState, "")
        GlideIM.getInstance().addConnectionListener(this)
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
        GlideIM.getInstance().removeConnectionListener(this)
    }

    open fun updateConnState(state: String) {

    }

    override fun onStateChange(state: Int, msg: String?) {
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