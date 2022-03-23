package pro.glideim.ui

import com.dengzii.ktx.android.content.update
import com.google.android.material.textview.MaterialTextView
import pro.glideim.BuildConfig
import pro.glideim.MessageListener
import pro.glideim.R
import pro.glideim.UserConfig
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.api.app.AppApi
import pro.glideim.sdk.utils.RxUtils
import pro.glideim.utils.UpdateUtils
import pro.glideim.utils.io2main
import pro.glideim.utils.request
import pro.glideim.utils.request2
import java.util.concurrent.TimeUnit

class SplashActivity : BaseActivity() {

    private val mTvState by lazy { findViewById<MaterialTextView>(R.id.tv_state) }

    override val layoutResId = R.layout.activity_splash

    override fun initView() {
        needAuth = false

        val checkAt = UserConfig(this).lastUpdateCheck.toLong()
        val spanMinutes = System.currentTimeMillis() - checkAt / 1000 / 60

        if (spanMinutes > 60) {
            AppApi.API.releaseInfo
                .timeout(5, TimeUnit.SECONDS)
                .map(RxUtils.bodyConverter())
                .doOnError {
                    tryLogin()
                }
                .request2(this) {
                    UserConfig(this).update {
                        lastUpdateCheck = (System.currentTimeMillis() / 1000).toString()
                    }
                    if (it.versionCode > BuildConfig.VERSION_CODE) {
                        UpdateUtils.showUpdate(this, it) { c ->
                            if (c) {
                                tryLogin()
                            }
                        }
                    } else {
                        tryLogin()
                    }
                }
        } else {
            tryLogin()
        }
    }

    private fun tryLogin() {
        GlideIM.authDefaultAccount()
            .io2main()
            .request {
                onStart {
                    // mTvState.text = "Connecting to server"
                }
                onSuccess {
                    GlideIM.getAccount().setImMessageListener(MessageListener.getInstance())
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