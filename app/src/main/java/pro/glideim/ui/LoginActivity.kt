package pro.glideim.ui

import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseActivity

class LoginActivity : BaseActivity() {

    private val mBtSubmit by lazy { findViewById<MaterialButton>(R.id.bt_submit) }
    private val mTvSignUp by lazy { findViewById<MaterialTextView>(R.id.tv_sign_up) }
    private val mTvResetPassword by lazy { findViewById<MaterialTextView>(R.id.tv_reset_password) }
    private val mEtPassword by lazy { findViewById<TextInputEditText>(R.id.et_password) }
    private val mEtAccount by lazy { findViewById<TextInputEditText>(R.id.et_account) }

    override val layoutResId = R.layout.activity_login

    override fun initView() {
        mBtSubmit.setOnClickListener {
            submit()
        }
        mTvSignUp.setOnClickListener {
            RegisterActivity.start(this)
        }
        mTvResetPassword.setOnClickListener {
            toast("Reset Password todo")
        }
    }

    private fun submit() {
        MainActivity.start(this)
        if (!validate()) {
            return
        }
        val account = mEtAccount.text.toString()
        val password = mEtPassword.text.toString()


    }

    private fun validate(): Boolean {

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