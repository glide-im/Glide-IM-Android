package pro.glideim.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.dengzii.adapter.SuperAdapter
import com.dengzii.ktx.android.content.intentExtra
import com.dengzii.ktx.android.toggleEnable
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.entity.IMMessage
import pro.glideim.sdk.entity.IMSession
import pro.glideim.sdk.entity.MessageChangeListener
import pro.glideim.sdk.protocol.ChatMessage
import pro.glideim.utils.io2main
import pro.glideim.utils.request
import pro.glideim.utils.request2
import java.util.*

class ChatActivity : BaseActivity() {

    private val mBtSend by lazy { findViewById<MaterialButton>(R.id.bt_send) }
    private val mEtMessage by lazy { findViewById<TextInputEditText>(R.id.et_message) }
    private val mRvMessages by lazy { findViewById<RecyclerView>(R.id.rv_messages) }
    private val mTvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }

    private val mMidMap: TreeMap<Long, IMMessage> = TreeMap()
    private val mMessage = MySortedList()
    private val mAdapter = SuperAdapter(mMessage)
    private val mLayoutManger = GridLayoutManager(this, 1, RecyclerView.VERTICAL, true)

    private lateinit var mSession: IMSession
    private val mUID by intentExtra(EXTRA_UID, 0L)
    private var mLastMid = 0L

    override val layoutResId = R.layout.activity_chat

    companion object {

        const val EXTRA_UID = "EXTRA_UID"

        @JvmStatic
        fun start(context: Context, uid: Long) {
            val starter = Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_UID, uid)
            }
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mSession = GlideIM.getInstance().account.imSessionList.getSession(1, mUID)
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        mMessage.l = SortedList(IMMessage::class.java, MessageListSorter(mAdapter))
        mEtMessage.toggleEnable()
        mBtSend.toggleEnable()

        mAdapter.addViewHolderForType(
            IMMessage::class.java,
            ChatMessageViewHolder::class.java
        )

        mRvMessages.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        mRvMessages.isNestedScrollingEnabled = false
        mRvMessages.layoutManager = mLayoutManger
        mRvMessages.adapter = mAdapter

        mBtSend.setOnClickListener {
            sendMessage()
        }
        setSessionInfo(mSession)
    }

    private fun setSessionInfo(s: IMSession) {
        mSession = s
        mEtMessage.toggleEnable()
        mBtSend.toggleEnable()

        mMessage.clear()

        val latest = mSession.getMessages(mLastMid, 20)
        mMessage.addAll(latest)
        scrollToLastMessage()

        mSession.setMessageListener(object : MessageChangeListener {
            override fun onChange(mid: Long, message: IMMessage) {}

            override fun onInsertMessage(mid: Long, message: IMMessage) {}

            override fun onNewMessage(message: IMMessage) {
                mMessage.add(message)
            }
        })
    }

    private fun sendMessage() {
        val msg = mEtMessage.text.toString()
        if (msg.isBlank()) {
            return
        }
        if (!GlideIM.getInstance().isConnected) {
            toast("IM server is disconnected")
            return
        }

        mBtSend.isEnabled = false

        GlideIM.sendChatMessage(mUID, 1, msg)
            .io2main()
            .request {
                onSuccess {
                    when (it.state) {
                        ChatMessage.STATE_INIT -> {
                        }
                        ChatMessage.STATE_CREATED -> {
                            mEtMessage.setText("")
                            mMessage.add(it)
                            scrollToLastMessage()
                        }
                        ChatMessage.STATE_SRV_RECEIVED -> {
                            mMessage.add(it)
                        }
                        ChatMessage.STATE_RCV_RECEIVED -> {
                            mMessage.add(it)
                        }
                        else -> {

                        }
                    }
                    mBtSend.isEnabled = true
                }
            }
    }

    private fun addMessage() {

    }

    private fun requestData() {
//        MsgApi.API.getChatMessageHistory(GetChatHistoryDto(mUID, 0))
//            .request(this) {
//                mMessage.clear()
//                mMessage.addAll(it!!.map {
//                    MessageViewData().apply {
//                        content = it.content
//                        time = "00:00"
//                        fromMe = GlideIM.getInstance().myUID == it.from
//                    }
//                })
//            }
    }

    override fun onResume() {
        super.onResume()
        GlideIM.getInstance().addConnectionListener { state, _ ->
            checkIMServerState()
        }
    }

    private fun checkIMServerState() {
        if (!GlideIM.getInstance().isConnected) {
            GlideIM.getInstance().tryReconnect()
                .io2main()
                .request2(this) {

                }
        }
    }

    private fun scrollToLastMessage() {
        if (mAdapter.itemCount > 0) {
            mRvMessages.scrollToPosition(0)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun updateConnState(state: String) {
        super.updateConnState(state)
        mTvTitle.text = mSession.title + " " + state
    }
}