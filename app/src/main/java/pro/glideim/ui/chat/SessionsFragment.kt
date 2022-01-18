import android.annotation.SuppressLint
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dengzii.adapter.SuperAdapter
import com.dengzii.ktx.android.content.getColorCompat
import com.dengzii.ktx.android.px2dp
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseFragment
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.IMSession
import pro.glideim.sdk.SessionUpdateListener
import pro.glideim.ui.chat.*
import pro.glideim.utils.*

class SessionsFragment : BaseFragment() {

    private val mTvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }

    private val mRvSessions by lazy { findViewById<RecyclerView>(R.id.rv_sessions) }
    private val mSrfRefresh by lazy { findViewById<SwipeRefreshLayout>(R.id.srf_refresh) }

    private val mSessionList = MySortedList<SessionViewData>()
    private val mAdapter = SuperAdapter(mSessionList)

    private val mIMSessionList by lazy { GlideIM.getAccount().imSessionList }

    companion object {
        private val TAG = SessionsFragment::class.java.simpleName
    }

    override val layoutRes = R.layout.fragment_session

    override fun initView() {
        mSessionList.l = SortedList(SessionViewData::class.java, SessionListSorter(mAdapter))
        mAdapter.addViewHolderForType(SessionViewData::class.java, SessionViewHolder::class.java)

//        mAdapter.setEnableEmptyView(true, SuperAdapter.EMPTY)
//        mAdapter.setEnableEmptyViewOnInit(true)
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

        mIMSessionList.setSessionUpdateListener(
            object : SessionUpdateListener {
                override fun onUpdate(session: IMSession) {
                    requireActivity().runOnUiThread {
                        val s = SessionViewData.create(session)
                        val updateAt = s.updateAt
                        s.updateAt = s.preUpdateAt

                        mSessionList.l.beginBatchedUpdates()
                        mSessionList.remove(s)
                        s.updateAt = updateAt
                        mSessionList.add(s)
                        mSessionList.l.endBatchedUpdates()
                    }
                }

                override fun onNewSession(session: IMSession) {
                    requireActivity().runOnUiThread {
                        mSessionList.add(SessionViewData.create(session))
                    }
                }
            })

        mSrfRefresh.startRefresh()
    }

    override fun onRequestFinish() {
        super.onRequestFinish()
        mSrfRefresh.finishRefresh()
    }

    private fun requestData() {
        mIMSessionList.initSessionsList()
            .io2main()
            .request(this) {
                mSessionList.addAll(mIMSessionList.sessions.map { s -> SessionViewData.create(s) })
            }
    }

    @SuppressLint("SetTextI18n")
    override fun updateConnState(state: String) {
        super.updateConnState(state)
        if (state.isBlank()) {
            mTvTitle.text = "Glide"
        } else {
            mTvTitle.text = state
        }
    }
}