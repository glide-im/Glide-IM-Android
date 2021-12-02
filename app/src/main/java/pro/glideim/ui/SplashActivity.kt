package pro.glideim.ui

import android.content.Intent
import pro.glideim.R
import pro.glideim.base.BaseActivity

class SplashActivity : BaseActivity() {

    override val layoutResId = R.layout.activity_splash

    override fun initView() {

        window.decorView.postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
        }, 1000)
    }
}