package pro.glideim.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.im.ConnStateListener
import pro.glideim.sdk.ws.WsClient
import pro.glideim.utils.RequestStateCallback
import pro.glideim.utils.io2main
import pro.glideim.utils.request2

abstract class BaseFragment : Fragment(), RequestStateCallback, ConnStateListener {

    private lateinit var mView: View

    abstract val layoutRes: Int

    abstract fun initView()

    private var inited = false

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
        onStateChange(GlideIM.getInstance().connState, "")
        GlideIM.getInstance().addConnectionListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
        activity?.runOnUiThread {
            updateConnState(s)
        }
    }
}