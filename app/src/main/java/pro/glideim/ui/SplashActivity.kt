package pro.glideim.ui

import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.UserPerf
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.utils.io2main
import pro.glideim.utils.request2

class SplashActivity : BaseActivity() {

    private val mTvState by lazy { findViewById<MaterialTextView>(R.id.tv_state) }

    override val layoutResId = R.layout.activity_splash

    override fun initView() {

        mTvState.postDelayed({
            if (UserPerf.getInstance().loadToken().isBlank()) {
                LoginActivity.start(this)
                finish()
            } else {
                checkToken()
            }
        }, 100)
    }

    private fun checkToken() {
        mTvState.text = "Sig in ..."
        GlideIM.auth()
            .io2main()
            .doOnError {
                LoginActivity.start(this)
                finish()
            }
            .request2(this) {
                if (it == true) {
                    MainActivity.start(this)
                } else {
                    LoginActivity.start(this)
                }
                finish()
            }
    }

    override fun onRequestError(t: Throwable) {
    }
}