import android.util.Log
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
import pro.glideim.sdk.entity.SessionUpdateListener
import pro.glideim.ui.chat.SessionViewHolder
import pro.glideim.utils.*

class SessionsFragment : BaseFragment() {

    private val mRvSessions by lazy { findViewById<RecyclerView>(R.id.rv_sessions) }
    private val mSrfRefresh by lazy { findViewById<SwipeRefreshLayout>(R.id.srf_refresh) }

    private val mSessionList = mutableListOf<Any>()
    private val mAdapter = SuperAdapter(mSessionList)

    private val mIMSessionList by lazy { GlideIM.getInstance().account.imSessionList }

    companion object {
        private val TAG = SessionsFragment::class.java.simpleName
    }

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

        mIMSessionList.setSessionUpdateListener(
            object : SessionUpdateListener {
                override fun onUpdate(vararg session: IMSession) {
                    Log.d(
                        TAG,
                        "onUpdate() called with: session = ${session.joinToString { "${it.to}-${it.title}" }}"
                    )
                    for (s in session) {
                        val indexOf = mSessionList.indexOf(s)
                        if (indexOf != -1) {
                            activity?.runOnUiThread {
                                mAdapter.notifyItemChanged(indexOf)
                            }
                        }
                    }
                }

                override fun onNewSession(vararg session: IMSession) {
                    Log.d(
                        TAG,
                        "onNewSession() called with: session = ${session.joinToString { "${it.to}-${it.title}" }}"
                    )
                    mSessionList.addAll(session)
                    mSessionList.sortBy {
                        -((it as? IMSession)?.updateAt ?: 0)
                    }
                    activity?.runOnUiThread {
                        mAdapter.notifyDataSetChanged()
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

            }
    }
}