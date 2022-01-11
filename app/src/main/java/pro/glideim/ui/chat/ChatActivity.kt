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
import com.dengzii.ktx.android.toggleEnable
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.IMMessage
import pro.glideim.sdk.IMSession
import pro.glideim.sdk.MessageChangeListener
import pro.glideim.sdk.protocol.ChatMessage
import pro.glideim.utils.io2main
import pro.glideim.utils.request

class ChatActivity : BaseActivity() {

    private val mBtSend by lazy { findViewById<MaterialButton>(R.id.bt_send) }
    private val mEtMessage by lazy { findViewById<TextInputEditText>(R.id.et_message) }
    private val mRvMessages by lazy { findViewById<RecyclerView>(R.id.rv_messages) }
    private val mTvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }

    private val mMessage = MySortedList<IMMessage>()
    private val mAdapter = SuperAdapter(mMessage)
    private val mLayoutManger = GridLayoutManager(this, 1, RecyclerView.VERTICAL, true)

    private lateinit var mSession: IMSession

    override val layoutResId = R.layout.activity_chat

    companion object {

        const val EXTRA_ID = "EXTRA_ID"
        const val EXTRA_TYPE = "EXTRA_TYPE"

        fun start(context: Context, s: IMSession) {
            start(context, s.to, s.type)
        }

        @JvmStatic
        fun start(context: Context, id: Long, type: Int) {
            val starter = Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_ID, id)
                putExtra(EXTRA_TYPE, type)
            }
            if (id == 0L || type == 0) {
                return
            }
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val id = intent.getLongExtra(EXTRA_ID, 0)
        val type = intent.getIntExtra(EXTRA_TYPE, 0)
        mSession = GlideIM.getAccount().imSessionList.getSession(type, id)
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

        val latest = mSession.getMessages(mSession.lastMsgId, 20)
        mMessage.addAll(latest)
        scrollToLastMessage()

        mSession.setMessageListener(object :
            MessageChangeListener {
            override fun onChange(mid: Long, message: IMMessage) {}

            override fun onInsertMessage(mid: Long, message: IMMessage) {}

            override fun onNewMessage(message: IMMessage) {
                runOnUiThread {
                    mMessage.add(message)
                    scrollToLastMessage()
                }
            }
        })
    }

    private fun sendMessage() {
        val msg = mEtMessage.text.toString()
        if (msg.isBlank()) {
            return
        }
        if (!GlideIM.getAccount().imClient.isConnected) {
            toast("IM server is disconnected")
            return
        }

        mBtSend.isEnabled = false

        mSession.sendTextMessage(msg)
            .io2main()
            .request {
                onError {
                    it.printStackTrace()
                    toast(it.localizedMessage ?: it.message ?: "send failed")
                    mEtMessage.setText("")
                    mBtSend.isEnabled = true
                }
                onSuccess {
                    mMessage.add(it)
                    when (it.state) {
                        ChatMessage.STATE_INIT -> {
                        }
                        ChatMessage.STATE_CREATED -> {
                            mEtMessage.setText("")
                            scrollToLastMessage()
                        }
                        ChatMessage.STATE_SRV_RECEIVED -> {
                        }
                        ChatMessage.STATE_RCV_RECEIVED -> {
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