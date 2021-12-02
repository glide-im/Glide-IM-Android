package pro.glideim.base

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

abstract class BaseActivity : Activity() {
    abstract val layoutResId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
    }

    override fun onStart() {
        super.onStart()
        initView()
    }

    abstract fun initView()

    fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}