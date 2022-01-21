package pro.glideim.ui.contacts

import android.content.Context
import android.content.Intent
import com.dengzii.ktx.android.content.intentExtra
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.api.user.ContactsUidDto
import pro.glideim.sdk.api.user.UserApi
import pro.glideim.ui.Events
import pro.glideim.utils.io2main
import pro.glideim.utils.request
import pro.glideim.utils.request2

class AddContactsActivity : BaseActivity() {
    private val mBtQrCode by lazy { findViewById<MaterialButton>(R.id.bt_qr_code) }
    private val mBtSearch by lazy { findViewById<MaterialButton>(R.id.bt_search) }
    private val mEtId by lazy { findViewById<TextInputEditText>(R.id.et_id) }

    private val mSearchGroup by intentExtra("group", false)

    override val layoutResId = R.layout.activity_add_contacts

    companion object {
        @JvmStatic
        fun start(context: Context, isGroup: Boolean) {
            val starter = Intent(context, AddContactsActivity::class.java).apply {
                putExtra("group", isGroup)
            }
            context.startActivity(starter)
        }
    }

    override fun initView() {
        if (mSearchGroup) {
            mEtId.hint = "Input Group ID"
        } else {
            mEtId.hint = "Input User ID"
        }
        mBtSearch.setOnClickListener {
            search()
        }
        mBtQrCode.setOnClickListener {
            toast("TODO")
        }
    }

    private fun search() {
        val id = mEtId.text.toString()
        if (id.toLongOrNull() == null || id.length < 2) {
            toast("check id")
            return
        }
        if (mSearchGroup) {
            GlideIM.getAccount().joinGroup(id.toLong())
                .io2main()
                .request2(this) {
                    toast("Group added")
                    Events.updateContacts()
                    finish()
                }
        } else {
            UserApi.API.addContacts(ContactsUidDto(id.toLong(), ""))
                .request(this) {
                    toast("Contacts added")
                    Events.updateContacts()
                    finish()
                }
        }
    }
}