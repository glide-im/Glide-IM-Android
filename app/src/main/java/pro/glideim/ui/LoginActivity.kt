package pro.glideim.ui

import android.content.Context
import android.content.Intent
import android.widget.ProgressBar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
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
            context.startActivity(starter)
        }
    }

    override fun initView() {
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

    private fun submit() {
        MainActivity.start(this)
        if (!validate()) {
            return
        }
        val account = mEtAccount.text.toString()
        val password = mEtPassword.text.toString()

        GlideIM.login(account, password, 1)
            .io2main()
            .request2(this) {
                MainActivity.start(this)
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