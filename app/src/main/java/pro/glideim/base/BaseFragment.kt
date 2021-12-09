package pro.glideim.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import pro.glideim.utils.RequestStateCallback

abstract class BaseFragment : Fragment(), RequestStateCallback {

    private lateinit var mView: View

    abstract val layoutRes: Int

    abstract fun initView()

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
        initView()
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
        toast(t.message ?: "error")
    }
}