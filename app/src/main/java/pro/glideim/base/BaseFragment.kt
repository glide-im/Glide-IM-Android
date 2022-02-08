package pro.glideim.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.IMAccount
import pro.glideim.sdk.im.ConnStateListener
import pro.glideim.sdk.ws.WsClient
import pro.glideim.utils.RequestStateCallback

abstract class BaseFragment : Fragment(), RequestStateCallback, ConnStateListener {

    private lateinit var mView: View

    abstract val layoutRes: Int

    abstract fun initView()

    private var inited = false

    protected val account: IMAccount? = GlideIM.getAccount()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(layoutRes, null)
        return mView
    }

    override fun onStart() {
        super.onStart()
        if (!inited) {
            initView()
            inited = true
        }
    }


    fun <T : View> findViewById(@IdRes id: Int): T {
        return mView.findViewById(id)
    }

    fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestStart() {

    }

    override fun onRequestFinish() {

    }

    override fun onRequestError(t: Throwable) {
        t.printStackTrace()
        toast(t.message ?: "error")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        account?.imClient?.apply {
            onStateChange(webSocketClient.state, "")
            addConnStateListener(this@BaseFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        account?.imClient?.apply {
            removeConnStateListener(this@BaseFragment)
        }
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
        activity?.runOnUiThread {
            updateConnState(s)
        }
    }
}