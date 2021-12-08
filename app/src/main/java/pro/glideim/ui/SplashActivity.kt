package pro.glideim.ui

import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.ui.chat.ChatActivity

class SplashActivity : BaseActivity() {

    override val layoutResId = R.layout.activity_splash

    override fun initView() {

        window.decorView.postDelayed({
            ChatActivity.start(this)
        }, 1000)
    }
}