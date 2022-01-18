package pro.glideim.ui

import android.content.Context
import android.content.Intent
import com.dengzii.ktx.android.content.update
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pro.glideim.R
import pro.glideim.UserConfig
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.utils.io2main
import pro.glideim.utils.request2

class RegisterActivity : BaseActivity() {

    private val mEtPasswordAgain by lazy { findViewById<TextInputEditText>(R.id.et_password_again) }
    private val mBtSubmit by lazy { findViewById<MaterialButton>(R.id.bt_submit) }
    private val mEtPassword by lazy { findViewById<TextInputEditText>(R.id.et_password) }
    private val mEtAccount by lazy { findViewById<TextInputEditText>(R.id.et_account) }


    override val layoutResId = R.layout.activity_register

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, RegisterActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun initView() {
        mBtSubmit.setOnClickListener {
            submit()
        }
    }

    private fun submit() {
        if (!validate()) {
            return
        }
        val account = mEtAccount.text.toString()
        val password = mEtPassword.text.toString()

        GlideIM.register(account, password)
            .io2main()
            .request2(this) {
                toast("Register success!")
                UserConfig(this).update {
                    this.account = account
                    this.password = password
                }
                finish()
            }
    }

    private fun validate(): Boolean {

        val account = mEtAccount.text.toString()
        val password = mEtPassword.text.toString()
        val passwordAgain = mEtPasswordAgain.text.toString()

        if (account.isBlank() || account.length < 5) {
            toast("Please check your account, must contain more than 5 characters")
            return false
        }
        if (password.isBlank() || password.length < 6) {
            toast("Please check your password, must contain more than 6 characters")
            return false
        }
        if (password != passwordAgain) {
            toast("The password is inconsistent")
            return false
        }

        return true
    }
}