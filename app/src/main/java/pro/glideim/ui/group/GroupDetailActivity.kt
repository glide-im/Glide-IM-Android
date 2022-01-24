package pro.glideim.ui.group

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dengzii.adapter.SuperAdapter
import com.dengzii.adapter.addViewHolderForType
import com.dengzii.ktx.android.content.intentExtra
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.Constants
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.api.group.GroupApi
import pro.glideim.sdk.api.group.RemoveMemberDto
import pro.glideim.sdk.api.user.ContactsUidDto
import pro.glideim.sdk.api.user.UserApi
import pro.glideim.ui.Events
import pro.glideim.utils.*

class GroupDetailActivity : BaseActivity() {

    private val mRvMembers by lazy { findViewById<RecyclerView>(R.id.rv_members) }
    private val mMembers = mutableListOf<GroupMemberViewData>()
    private val mAdapter = SuperAdapter(mMembers)

    override val layoutResId = R.layout.activity_group_detail

    private var mOwnerUID = 0L
    private val mGid by intentExtra("gid", 0L)

    companion object {
        @JvmStatic
        fun start(context: Context, gid: Long) {
            val starter = Intent(context, GroupDetailActivity::class.java)
                .putExtra("gid", gid)
            context.startActivity(starter)
        }
    }

    override fun initView() {
        if (mGid == 0L) {
            finish()
        }
        mAdapter.addViewHolderForType<GroupMemberViewData>(R.layout.item_group_member) {
            val ivAvatar = findView<AppCompatImageView>(R.id.iv_avatar)
            val tvNickname = findView<MaterialTextView>(R.id.tv_nickname)
            val tvType = findView<MaterialTextView>(R.id.tv_type)
            val llContainer = findView<ViewGroup>(R.id.ll_container)
            onBindData { data, index ->
                llContainer.setOnClickListener {
                    onItemClicked(index, data.memberInfo.uid)
                }
                ivAvatar.loadImageClipCircle(data.userInfo.avatar)
                val type = when (data.memberInfo.type) {
                    1 -> "Owner"
                    2 -> "Admin"
                    else -> ""
                }
                tvNickname.text = "${data.userInfo.nickname} (${data.userInfo.uid})"
                tvType.text = type
            }
        }
        mRvMembers.adapter = mAdapter
        mRvMembers.layoutManager = LinearLayoutManager(this)

        loadMembers()

    }

    private fun loadMembers() {

        val group = GlideIM.getAccount().getContact(Constants.SESSION_TYPE_GROUP, mGid)
        if (group == null) {
            finish()
        }
        val mbs = group.members.associateBy { it.uid }
        GlideIM.getUserInfo(mbs.keys.toList())
            .io2main()
            .request2(this) {
                it.forEach { uf ->
                    val mb = mbs[uf.uid]!!
                    val index = when (mb.type) {
                        1 -> 0
                        2 -> if (mMembers.firstOrNull()?.memberInfo?.type == 1) 1 else 0
                        else -> mMembers.size
                    }
                    mMembers.add(index, GroupMemberViewData(mb, uf))
                }
                if (mMembers.size > 0) {
                    mOwnerUID = mMembers.first().memberInfo.uid
                    mAdapter.notifyItemRangeInserted(0, mMembers.size)
                }
            }
    }

    private fun onItemClicked(index: Int, uid: Long) {
        if (uid == account?.uid) {
            return
        }

        val item = mutableListOf<String>()
        if (account?.uid == mOwnerUID) {
            item.add("Remove")
        }
        if (account?.getContact(Constants.SESSION_TYPE_USER, uid) == null) {
            item.add("Add Contacts")
        }

        if (item.isEmpty()) {
            return
        }
        MaterialAlertDialogBuilder(this).apply {
            setItems(item.toTypedArray()) { _, i ->
                when (item[i]) {
                    "Remove" -> {
                        GroupApi.API.removeMember(RemoveMemberDto(listOf(uid)))
                            .io2main()
                            .request(this@GroupDetailActivity) {
                                mMembers.removeAt(index)
                                mAdapter.notifyItemRemoved(index)
                                toast("Member Removed")
                            }
                    }
                    "Add Contacts" -> {
                        UserApi.API.addContacts(ContactsUidDto(uid, ""))
                            .request(this@GroupDetailActivity) {
                                toast("Contacts added")
                                Events.updateContacts()
                            }
                    }
                }
            }
            create().show()
        }
    }
}