package pro.glideim.ui.chat

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dengzii.adapter.SuperAdapter
import com.dengzii.ktx.android.content.getColorCompat
import com.dengzii.ktx.android.px2dp
import pro.glideim.R
import pro.glideim.base.BaseFragment
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.entity.IMSession
import pro.glideim.utils.*

class SessionsFragment : BaseFragment() {

    private val mRvSessions by lazy { findViewById<RecyclerView>(R.id.rv_sessions) }
    private val mSrfRefresh by lazy { findViewById<SwipeRefreshLayout>(R.id.srf_refresh) }

    private val mSessionList = mutableListOf<Any>()
    private val mAdapter = SuperAdapter(mSessionList)

    override val layoutRes = R.layout.fragment_session

    override fun initView() {

        mAdapter.addViewHolderForType(IMSession::class.java, SessionViewHolder::class.java)

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

        mSrfRefresh.startRefresh()
    }

    override fun onRequestFinish() {
        super.onRequestFinish()
        mSrfRefresh.finishRefresh()
    }

    private fun requestData() {
        GlideIM.getSessionList()
            .io2main()
            .request2(this) {
                mSessionList.clear()
                mSessionList.addAll(it!!)
                mAdapter.notifyDataSetChanged()

                GlideIM.updateRecentMessage()
                    .io2main()
                    .request2(this) {
                        mAdapter.notifyDataSetChanged()
                    }
            }
    }
}