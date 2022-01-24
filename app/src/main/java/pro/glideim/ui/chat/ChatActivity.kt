package pro.glideim.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.NotificationUtils
import com.dengzii.adapter.SuperAdapter
import com.dengzii.ktx.android.content.getDrawableCompat
import com.dengzii.ktx.android.gone
import com.dengzii.ktx.android.toggleEnable
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.vanniktech.emoji.EmojiPopup
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.*
import pro.glideim.sdk.messages.ChatMessage
import pro.glideim.ui.group.GroupDetailActivity
import pro.glideim.utils.io2main
import pro.glideim.utils.request
import pro.glideim.utils.request2

class ChatActivity : BaseActivity() {

    private val mBtSend by lazy { findViewById<MaterialButton>(R.id.bt_send) }
    private val mEtMessage by lazy { findViewById<TextInputEditText>(R.id.et_message) }
    private val mRvMessages by lazy { findViewById<RecyclerView>(R.id.rv_messages) }
    private val mTvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }
    private val mBtMore by lazy { findViewById<MaterialButton>(R.id.bt_more) }
    private val mBtEmoji by lazy { findViewById<MaterialButton>(R.id.bt_emoji) }
    private val mBtAdd by lazy { findViewById<MaterialButton>(R.id.bt_add) }

    private val mMessage = MySortedList<ChatMessageViewData>()
    private val mAdapter = SuperAdapter(mMessage)

    private val mEmojiPopup by lazy {
        EmojiPopup.Builder.fromRootView(mEtMessage).build(mEtMessage)
    }
    private lateinit var mSession: IMSession

    override val layoutResId = R.layout.activity_chat

    companion object {

        private const val TAG = "ChatActivity"
        private const val EXTRA_ID = "EXTRA_ID"
        private const val EXTRA_TYPE = "EXTRA_TYPE"

        private var current: IMSession? = null

        fun start(context: Context, s: IMSession) {
            start(context, s.to, s.type)
        }

        @JvmStatic
        fun start(context: Context, id: Long, type: Int) {
            if (id == 0L || type == 0) {
                return
            }
            context.startActivity(getIntent(context, id, type))
        }

        fun getIntent(context: Context, id: Long, type: Int): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_ID, id)
                putExtra(EXTRA_TYPE, type)
            }
        }

        fun getCurrentSession(): IMSession? {
            return current
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val id = intent.getLongExtra(EXTRA_ID, 0)
        val type = intent.getIntExtra(EXTRA_TYPE, 0)
        mSession = GlideIM.getAccount().imSessionList.getSession(type, id)
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        mMessage.l = SortedList(ChatMessageViewData::class.java, MessageListSorter(mAdapter))
        mEtMessage.toggleEnable()
        mBtSend.toggleEnable()

        mAdapter.addViewHolderForType(
            ChatMessageViewData::class.java,
            ChatMessageViewHolder::class.java
        )

        mRvMessages.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        mRvMessages.isNestedScrollingEnabled = false
        mRvMessages.layoutManager = GridLayoutManager(this, 1, RecyclerView.VERTICAL, true)
        mRvMessages.adapter = mAdapter

        mBtEmoji.setOnClickListener {
            mEmojiPopup.toggle()
            mBtEmoji.setIconResource(if (mEmojiPopup.isShowing) R.drawable.ic_keyboard else R.drawable.ic_emoji)
            mBtEmoji.invalidate()
        }
        mEtMessage.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            Log.d(TAG, "initView: $hasFocus")
        }
        mBtMore.setOnClickListener {
            onMoreClicked()
        }
        mBtSend.setOnClickListener {
            sendMessage()
        }
        if (mSession.type != Constants.SESSION_TYPE_GROUP) {
            mBtMore.gone()
        }
        mSession.clearUnread()
        mEtMessage.toggleEnable()
        mBtSend.toggleEnable()

        mMessage.clear()

        val latest = mSession.getMessages(Long.MAX_VALUE, 20)

        latest.forEach {
            addMessage(it)
        }
        scrollToLastMessage()
    }

    private fun sendMessage() {
        val msg = mEtMessage.text.toString()
        if (msg.isBlank()) {
            return
        }
        if (!GlideIM.getAccount().isIMAvailable) {
            toast("IM server is disconnected")
            return
        }

        mEtMessage.setText("")
        mSession.sendTextMessage(msg)
            .io2main()
            .request {
                onError {
                    it.printStackTrace()
                    toast(it.localizedMessage ?: it.message ?: "send failed")
                }
                onSuccess {
                    when (it.state) {
                        ChatMessage.STATE_INIT -> {
                        }
                        ChatMessage.STATE_CREATED -> {
                            addMessage(it)
                            scrollToLastMessage()
                        }
                        ChatMessage.STATE_RCV_FAILED,
                        ChatMessage.STATE_SRV_FAILED,
                        ChatMessage.STATE_SRV_RECEIVED,
                        ChatMessage.STATE_RCV_RECEIVED ->
                            addMessage(it)
                        else -> {
                        }
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        NotificationUtils.cancel(mSession.to.hashCode())
        current = mSession
        mSession.setMessageListener(object :
            MessageChangeListener {
            override fun onChange(mid: Long, message: IMMessage) {

            }

            override fun onInsertMessage(mid: Long, message: IMMessage) {
                runOnUiThread {
                    addMessage(message)
                }
                mSession.clearUnread()
            }

            override fun onNewMessage(message: IMMessage) {
                runOnUiThread {
                    mSession.clearUnread()
                    addMessage(message)
                    scrollToLastMessage()
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        current = null
        mSession.setMessageListener(null)
    }

    private fun addMessage(m: IMMessage) {
        val c = ChatMessageViewData(true, m)
        mMessage.add(c)
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

    private fun onMoreClicked() {
        val popupMenu = PopupMenu(this, mBtMore)
        popupMenu.gravity = Gravity.BOTTOM
        popupMenu.menuInflater.inflate(R.menu.menu_chat_group, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.item_members -> GroupDetailActivity.start(this, mSession.to)
                R.id.item_invite -> {
                }
                R.id.item_exit -> deleteContact()
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    private fun deleteContact() {
        GlideIM.getAccount().deleteContacts(mSession.type, mSession.to)
            .io2main()
            .request2(this) {
                finish()
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