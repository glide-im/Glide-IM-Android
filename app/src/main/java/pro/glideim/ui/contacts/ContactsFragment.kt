package pro.glideim.ui.contacts

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dengzii.adapter.SuperAdapter
import com.dengzii.adapter.addViewHolderForType
import com.dengzii.ktx.android.content.getColorCompat
import com.dengzii.ktx.android.px2dp
import com.dengzii.ktx.android.showWithLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseFragment
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.IMContacts
import pro.glideim.ui.Events
import pro.glideim.ui.chat.ChatActivity
import pro.glideim.utils.*

class ContactsFragment : BaseFragment() {
    private val mBtAdd by lazy { findViewById<MaterialButton>(R.id.bt_add) }

    private val mTvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }
    private val mRvSessions by lazy { findViewById<RecyclerView>(R.id.rv_contacts) }
    private val mSrfRefresh by lazy { findViewById<SwipeRefreshLayout>(R.id.srf_refresh) }

    private val mContacts = mutableListOf<Any>()
    private val mAdapter = SuperAdapter(mContacts)

    override val layoutRes = R.layout.fragment_contacts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BusUtils.register(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        BusUtils.unregister(this)
    }

    override fun initView() {

        mAdapter.addViewHolderForType<IMContacts>(R.layout.item_contacts) {
            val ivAvatar = findView<ImageView>(R.id.iv_avatar)
            val tvNickname = findView<MaterialTextView>(R.id.tv_nickname)
            onBindData { data, _ ->
                ivAvatar.loadImage(data.avatar)
                tvNickname.text = "${data.title} (${data.id})"
                itemView.setOnClickListener {
                    startChat(data.id, data.type)
                }
            }
        }
        mAdapter.setEnableEmptyView(true, SuperAdapter.EMPTY)
        mAdapter.setEnableEmptyViewOnInit(true)
        mRvSessions.adapter = mAdapter
        mRvSessions.layoutManager = LinearLayoutManager(requireContext())
        mRvSessions.addItemDecoration(
            ItemDecorationFactory.createDivider(
                1f.px2dp(),
                requireContext().getColorCompat(R.color.divider),
                60f,
                0f
            )
        )
        mSrfRefresh.onRefresh {
            requestData()
        }
        mBtAdd.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), mBtAdd)
            popupMenu.gravity = Gravity.BOTTOM
            popupMenu.menuInflater.inflate(R.menu.menu_contacts_add, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.item_add_friend -> AddContactsActivity.start(requireContext(), false)
                    R.id.item_add_group -> AddContactsActivity.start(requireContext(), true)
                    R.id.item_create_group -> showCreateGroup()
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }

        mSrfRefresh.startRefresh()
    }

    override fun onRequestFinish() {
        super.onRequestFinish()
        mSrfRefresh.finishRefresh()
    }

    @BusUtils.Bus(tag = Events.EVENT_UPDATE_CONTACTS, sticky = false)
    fun updateContacts() {
        mSrfRefresh.startRefresh()
    }

    private fun startChat(id: Long, type: Int) {
        if (type == 2) {
            ChatActivity.start(requireContext(), id, type)
            return
        }
        GlideIM.getAccount().imSessionList.getSession(id, type)
            .io2main()
            .request2(this) {
                when (type) {
                    1 -> ChatActivity.start(requireContext(), id, type)
                    else -> toast("TODO")
                }
            }

    }

    private fun requestData() {

        GlideIM.getAccount().contacts
            .io2main()
            .request2(this) {
                mContacts.clear()
                mContacts.addAll(it!!)
                mAdapter.notifyDataSetChanged()
                mSrfRefresh.finishRefresh()
            }
    }

    private fun showCreateGroup() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Create Group")
            val et = TextInputEditText(requireContext())
            et.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            et.hint = "Input Group Name"
            setView(et)

            setPositiveButton("Create") { d, _ ->
                if (createGroup(et.text.toString())) {
                    d.dismiss()
                }
            }
            setNegativeButton("Cancel") { d, _ ->
                d.dismiss()
            }
            create().showWithLifecycle(this@ContactsFragment)
        }

    }

    private fun createGroup(name: String): Boolean {
        if (name.trim().isBlank()) {
            return false
        }

        GlideIM.createGroup(name.trim())
            .io2main()
            .request2(this) {
                updateContacts()
            }
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun updateConnState(state: String) {
        super.updateConnState(state)
        if (state.isBlank()) {
            mTvTitle.text = "Contacts"
        } else {
            mTvTitle.text = state
        }
    }
}