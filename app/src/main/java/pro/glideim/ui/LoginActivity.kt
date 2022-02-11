package pro.glideim.ui

import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.ActivityUtils
import com.dengzii.ktx.android.content.update
import com.dengzii.ktx.android.content.use
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import pro.glideim.MessageListener
import pro.glideim.R
import pro.glideim.UserConfig
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.utils.io2main
import pro.glideim.utils.request2

class LoginActivity : BaseActivity() {

    private val mBtSubmit by lazy { findViewById<MaterialButton>(R.id.bt_submit) }
    private val mTvSignUp by lazy { findViewById<MaterialTextView>(R.id.tv_sign_up) }
    private val mTvResetPassword by lazy { findViewById<MaterialTextView>(R.id.tv_reset_password) }
    private val mEtPassword by lazy { findViewById<TextInputEditText>(R.id.et_password) }
    private val mEtAccount by lazy { findViewById<TextInputEditText>(R.id.et_account) }

    override val layoutResId = R.layout.activity_login

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, LoginActivity::class.java)
            starter.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            context.startActivity(starter)
            ActivityUtils.finishOtherActivities(LoginActivity::class.java)
        }
    }

    override fun initView() {
        ActivityUtils.getActivityList().forEach {
            if (it != this) {
                it.finish()
            }
        }
        needAuth = false
        mBtSubmit.setOnClickListener {
            submit()
        }
        mTvSignUp.setOnClickListener {
            RegisterActivity.start(this)
        }
        mTvResetPassword.setOnClickListener {
            toast("TODO")
        }
    }

    override fun onResume() {
        super.onResume()
        UserConfig(this).use {
            mEtAccount.setText(account)
            mEtPassword.setText(password)
        }
    }

    private fun submit() {
        if (!validate()) {
            return
        }
        val account = mEtAccount.text.toString()
        val password = mEtPassword.text.toString()

        GlideIM.login(account, password, 1)
            .io2main()
            .request2(this) {
                GlideIM.getAccount().setImMessageListener(MessageListener.getInstance())
                UserConfig(this).update {
                    this.account = account
                    this.password = password
                }
                MainActivity.start(this)
                finish()
            }
    }

    private fun validate(): Boolean {
        return true

        val account = mEtAccount.text.toString()
        val password = mEtPassword.text.toString()
        if (account.isBlank() || account.length < 5) {
            toast("Please check your account, must contain more than 5 characters")
            return false
        }
        if (password.isBlank() || password.length < 6) {
            toast("Please check your password, must contain more than 6 characters")
            return false
        }

        return true
    }
}