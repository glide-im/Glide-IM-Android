package pro.glideim.ui.chat

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dengzii.adapter.SuperAdapter
import com.dengzii.ktx.android.content.getColorCompat
import com.dengzii.ktx.android.px2dp
import pro.glideim.R
import pro.glideim.base.BaseFragment
import pro.glideim.utils.ItemDecorationFactory
import pro.glideim.utils.finishRefresh

class SessionsFragment : BaseFragment() {

    private val mRvSessions by lazy { findViewById<RecyclerView>(R.id.rv_sessions) }
    private val mSrfRefresh by lazy { findViewById<SwipeRefreshLayout>(R.id.srf_refresh) }

    private val mSessionLise = mutableListOf<Any>()
    private val mAdapter = SuperAdapter(mSessionLise)

    override val layoutRes = R.layout.fragment_session

    override fun initView() {


        mSessionLise.add(
            SessionViewData(
                avatar = "https://dengzii.com/static/a.webp",
                title = "Jack Tommy",
                msg = "Hello, how are you",
                unread = 1,
                time = "10:29"
            )
        )
        mSessionLise.add(
            SessionViewData(
                avatar = "https://dengzii.com/static/b.webp",
                title = "Adved Tommy",
                msg = "Yes",
                unread = 2,
                time = "11:29"
            )
        )
        mSessionLise.add(
            SessionViewData(
                avatar = "https://dengzii.com/static/c.webp",
                title = "Donal Jussy",
                msg = "Hello, how are you",
                unread = 0,
                time = "10:29"
            )
        )
        mSessionLise.add(
            SessionViewData(
                avatar = "https://dengzii.com/static/d.webp",
                title = "Jack Tommy",
                msg = "Hello, how are you",
                unread = 0,
                time = "10:29"
            )
        )
        mSessionLise.add(
            SessionViewData(
                avatar = "https://dengzii.com/static/a.webp",
                title = "Jack Tommy",
                msg = "Hello, how are you",
                unread = 0,
                time = "10:29"
            )
        )


        mAdapter.addViewHolderForType(SessionViewData::class.java, SessionViewHolder::class.java)

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

        mSrfRefresh.setOnRefreshListener {
            view?.postDelayed({

                mSrfRefresh.finishRefresh()

            }, 1000)
        }
    }
}