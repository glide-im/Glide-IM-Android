package pro.glideim.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pro.glideim.utils.RequestStateCallback

abstract class BaseActivity : AppCompatActivity(), RequestStateCallback{
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
}