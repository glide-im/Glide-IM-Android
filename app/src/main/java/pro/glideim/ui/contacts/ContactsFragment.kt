package pro.glideim.ui.contacts

import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dengzii.adapter.SuperAdapter
import com.dengzii.adapter.addViewHolderForType
import com.dengzii.ktx.android.content.getColorCompat
import com.dengzii.ktx.android.px2dp
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseFragment
import pro.glideim.sdk.api.Response
import pro.glideim.sdk.api.msg.GetSessionDto
import pro.glideim.sdk.api.msg.MsgApi
import pro.glideim.sdk.api.user.GetUserInfoDto
import pro.glideim.sdk.api.user.UserApi
import pro.glideim.ui.Events
import pro.glideim.ui.chat.ChatActivity
import pro.glideim.utils.*

class ContactsFragment : BaseFragment() {
    private val mBtAdd by lazy { findViewById<MaterialButton>(R.id.bt_add) }

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

        mAdapter.addViewHolderForType<ContactsViewData>(R.layout.item_contacts) {
            val ivAvatar = findView<ImageView>(R.id.iv_avatar)
            val tvNickname = findView<MaterialTextView>(R.id.tv_nickname)
            onBindData { data, _ ->
                ivAvatar.loadImage(data.avatar)
                tvNickname.text = "${data.nickname} (${data.id})"
                itemView.setOnClickListener {
                    if (data.type == 1) {
                        startChat(data.id)
                    }
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
            AddContactsActivity.start(requireContext())
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

    private fun startChat(uid: Long) {
        MsgApi.API.getSession(GetSessionDto(uid))
            .request(this) {
                ChatActivity.start(requireContext(), uid)
            }
    }

    private fun requestData() {

        UserApi.API.contactsList
            .flatMap {
                if (it.code != 100) {
                    throw Exception("${it.code}, ${it.msg}")
                }
                val uid = mutableListOf<Long>()
                val gid = mutableListOf<Long>()
                it.data.forEach { c ->
                    when (c.type) {
                        1 -> uid.add(c.id)
                        2 -> gid.add(c.id)
                    }
                }
                UserApi.API.getUserInfo(GetUserInfoDto(uid))
            }
            .map {
                Response<List<ContactsViewData>>().apply {
                    code = it.code
                    msg = it.msg
                    data = it.data.map {
                        ContactsViewData(it.uid, it.avatar, it.nickname, 1)
                    }
                }
            }
            .request(this) {
                mContacts.clear()
                mContacts.addAll(it!!)
                mAdapter.notifyDataSetChanged()
                mSrfRefresh.finishRefresh()
            }
    }
}