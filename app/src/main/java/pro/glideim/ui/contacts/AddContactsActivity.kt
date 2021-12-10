package pro.glideim.ui.contacts

import android.content.Context
import android.content.Intent
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.api.user.ContactsUidDto
import pro.glideim.sdk.api.user.UserApi
import pro.glideim.ui.Events
import pro.glideim.utils.BusUtils
import pro.glideim.utils.request

class AddContactsActivity : BaseActivity() {
    private val mBtQrCode by lazy { findViewById<MaterialButton>(R.id.bt_qr_code) }
    private val mBtSearch by lazy { findViewById<MaterialButton>(R.id.bt_search) }
    private val mEtId by lazy { findViewById<TextInputEditText>(R.id.et_id) }

    override val layoutResId = R.layout.activity_add_contacts

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, AddContactsActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun initView() {
        mBtSearch.setOnClickListener {
            search()
        }
        mBtQrCode.setOnClickListener {
            toast("TODO")
        }
    }

    private fun search() {
        val id = mEtId.text.toString()
        if (id.toLongOrNull() == null || id.length < 5) {
            toast("check id")
            return
        }
        UserApi.API.addContacts(ContactsUidDto(id.toLong(), ""))
            .request(this) {
                toast("Contacts added")
                BusUtils.post(Events.EVENT_UPDATE_CONTACTS)
                finish()
            }
    }
}