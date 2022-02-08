package pro.glideim.ui.contacts

import android.content.Intent
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dengzii.adapter.SuperAdapter
import com.dengzii.adapter.addViewHolderForType
import com.dengzii.ktx.android.antiShakeClick
import com.dengzii.ktx.android.content.intentExtra
import com.dengzii.ktx.android.content.startActivityForResult
import com.dengzii.ktx.android.hide
import com.dengzii.ktx.android.show
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.Constants
import pro.glideim.sdk.IMContacts
import pro.glideim.utils.loadImageRoundCorners

class SelectContactsActivity : BaseActivity() {

    private val mRvContacts by lazy { findViewById<RecyclerView>(R.id.rv_contacts) }
    private val mBtComplete by lazy { findViewById<MaterialButton>(R.id.bt_complete) }
    private val mTvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }

    private val mExcludes by intentExtra("excludes", longArrayOf())
    private val mTitle by intentExtra("title", "Select Contacts")
    private val mType by intentExtra("type", TYPE_ALL)

    private val mContacts = mutableListOf<IMContacts>()
    private val mAdapter = SuperAdapter(mContacts)
    private val mSelected = mutableListOf<IMContacts>()

    override val layoutResId = R.layout.activity_select_contacts

    companion object {

        const val TYPE_USER = Constants.SESSION_TYPE_USER
        const val TYPE_GROUP = Constants.SESSION_TYPE_GROUP
        private const val TYPE_ALL = TYPE_USER + TYPE_GROUP

        @JvmStatic
        fun startForResult(
            context: AppCompatActivity,
            title: String,
            type: Int,
            excludes: List<Long>,
            callback: (selected: Array<Long>) -> Unit
        ) {
            val starter = Intent(context, SelectContactsActivity::class.java)
                .putExtra("excludes", excludes.toLongArray())
                .putExtra("title", title)
                .putExtra("type", type)
            context.startActivityForResult(starter) { _, _, data ->
                val result = data?.getLongArrayExtra("selected")
                callback(result?.toTypedArray() ?: arrayOf())
            }
        }
    }

    override fun initView() {
        mTvTitle.text = mTitle

        val filter = account?.tempContacts.orEmpty().filter {
            if (mType == TYPE_ALL) true else it.type == mType && !mExcludes.contains(it.id)
        }

        mContacts.addAll(filter)
        mAdapter.addViewHolderForType<IMContacts>(R.layout.item_select_contacts) {
            val ivAvatar = findView<ImageView>(R.id.iv_avatar)
            val ivCheck = findView<ImageView>(R.id.iv_check)
            val tvNickname = findView<MaterialTextView>(R.id.tv_nickname)
            val vgContainer = findView<ViewGroup>(R.id.vg_container)
            onBindData { data, _ ->
                ivAvatar.loadImageRoundCorners(data.avatar, 6f)
                tvNickname.text = "${data.title} (${data.id})"
                if (mSelected.contains(data)) {
                    ivCheck.show()
                } else {
                    ivCheck.hide()
                }
                vgContainer.setOnClickListener {
                    if (mSelected.contains(data)) {
                        mSelected.remove(data)
                        ivCheck.hide()
                    } else {
                        mSelected.add(data)
                        ivCheck.show()
                    }
                }
            }
        }
        mRvContacts.layoutManager = LinearLayoutManager(this)
        mRvContacts.adapter = mAdapter

        mBtComplete.antiShakeClick {
            val selected = Intent().putExtra("selected", mSelected.map { it.id }.toLongArray())
            setResult(1, selected)
            finish()
        }
    }
}