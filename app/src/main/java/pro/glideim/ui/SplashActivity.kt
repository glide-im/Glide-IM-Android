package pro.glideim.ui

import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.UserPerf
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.utils.io2main
import pro.glideim.utils.request
import pro.glideim.utils.request2

class SplashActivity : BaseActivity() {

    private val mTvState by lazy { findViewById<MaterialTextView>(R.id.tv_state) }

    override val layoutResId = R.layout.activity_splash

    override fun initView() {

        GlideIM.getInstance().connect()
            .io2main()
            .request {
                onStart {
                    mTvState.text = "Connecting to server"
                }
                onSuccess {
                    checkToken()
                }
                onError {
                    it.printStackTrace()
                    toast(it.message ?: it.localizedMessage)
                }
            }
    }

    private fun checkToken() {
        val token = UserPerf.getInstance().loadToken()
        if (token.isBlank()) {
            LoginActivity.start(this)
            finish()
            return
        }

        mTvState.text = "Logging in ..."
        GlideIM.auth()
            .io2main()
            .doOnError {
                LoginActivity.start(this)
                finish()
            }
            .request2(this) {
                MainActivity.start(this)
                finish()
            }
    }

    override fun onRequestError(t: Throwable) {
        t.printStackTrace()
    }
}