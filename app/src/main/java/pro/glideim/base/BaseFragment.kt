package pro.glideim.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

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
}