package pro.glideim.ui

import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.IMAccount
import pro.glideim.utils.io2main
import pro.glideim.utils.request

class SplashActivity : BaseActivity() {

    private val mTvState by lazy { findViewById<MaterialTextView>(R.id.tv_state) }

    override val layoutResId = R.layout.activity_splash

    override fun initView() {

        GlideIM.authDefaultAccount()
            .io2main()
            .request {
                onStart {
                    mTvState.text = "Connecting to server"
                }
                onSuccess {
                    MainActivity.start(this@SplashActivity)
                    finish()
                }
                onError {
                    it.printStackTrace()
                    LoginActivity.start(this@SplashActivity)
                    finish()
                }
            }
    }

    override fun onRequestError(t: Throwable) {
        t.printStackTrace()
    }
}