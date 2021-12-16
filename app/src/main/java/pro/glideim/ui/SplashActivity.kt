package pro.glideim.ui

import android.widget.ProgressBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pro.glideim.R
import pro.glideim.UserPerf
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.utils.io2main
import pro.glideim.utils.request2

class SplashActivity : BaseActivity() {

    override val layoutResId = R.layout.activity_splash

    override fun initView() {

        val progressBar = ProgressBar(this)
        var loading = MaterialAlertDialogBuilder(this)
            .setView(progressBar)
            .setCancelable(true)
            .create()
        GlideIM.login("abc", "abc", 1)
            .io2main()
            .request2(this) {

            }

        if (UserPerf.getInstance().loadToken().isBlank()) {
            LoginActivity.start(this)
            finish()
        } else {

        }
        window.decorView.postDelayed({

            MainActivity.start(this)
            finish()
        }, 1000)
    }
}