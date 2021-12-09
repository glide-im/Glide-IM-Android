package pro.glideim.ui.contacts

import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dengzii.adapter.SuperAdapter
import com.dengzii.adapter.addViewHolderForType
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseFragment
import pro.glideim.sdk.api.user.GetUserInfoDto
import pro.glideim.sdk.api.user.UserApi
import pro.glideim.utils.finishRefresh
import pro.glideim.utils.loadImage
import pro.glideim.utils.request

class ContactsFragment : BaseFragment() {
    private val mBtAdd by lazy { findViewById<MaterialButton>(R.id.bt_add) }

    private val mRvSessions by lazy { findViewById<RecyclerView>(R.id.rv_contacts) }
    private val mSrfRefresh by lazy { findViewById<SwipeRefreshLayout>(R.id.srf_refresh) }

    private val mContacts = mutableListOf<Any>()
    private val mAdapter = SuperAdapter(mContacts)

    override val layoutRes = R.layout.fragment_contacts

    override fun initView() {

        mAdapter.addViewHolderForType<ContactsViewData>(R.layout.item_contacts) {
            val ivAvatar = findView<ImageView>(R.id.iv_avatar)
            val tvNickname = findView<MaterialTextView>(R.id.tv_nickname)
            onBindData { data, _ ->
                ivAvatar.loadImage(data.avatar)
                tvNickname.text = data.nickname
            }
        }
        mAdapter.setEnableEmptyView(true, SuperAdapter.EMPTY)
        mAdapter.setEnableEmptyViewOnInit(true)
        mRvSessions.adapter = mAdapter
        mRvSessions.layoutManager = LinearLayoutManager(requireContext())

        mSrfRefresh.setOnRefreshListener {
            requestData()
        }
        mBtAdd.setOnClickListener {
            AddContactsActivity.start(requireContext())
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
            }.request(this) {
                mContacts.clear()
                mContacts.addAll(it!!)
                mAdapter.notifyDataSetChanged()
                mSrfRefresh.finishRefresh()
            }
    }
}