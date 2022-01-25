package pro.glideim.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.NotificationUtils
import com.dengzii.adapter.SuperAdapter
import com.dengzii.ktx.android.gone
import com.dengzii.ktx.android.show
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
    private val mBtMenu by lazy { findViewById<MaterialButton>(R.id.bt_more) }
    private val mBtEmoji by lazy { findViewById<MaterialButton>(R.id.bt_emoji) }
    private val mBtAdd by lazy { findViewById<MaterialButton>(R.id.bt_add) }
    private val mFlMultiMsg by lazy { findViewById<ViewGroup>(R.id.fl_multi_message) }


    private val mBtLocation by lazy { findViewById<MaterialButton>(R.id.bt_location) }
    private val mBtVote by lazy { findViewById<MaterialButton>(R.id.bt_vote) }
    private val mBtFile by lazy { findViewById<MaterialButton>(R.id.bt_file) }
    private val mBtVoice by lazy { findViewById<MaterialButton>(R.id.bt_voice) }
    private val mBtCamera by lazy { findViewById<MaterialButton>(R.id.bt_camera) }
    private val mBtImage by lazy { findViewById<MaterialButton>(R.id.bt_image) }

    private val mMessage = MySortedList<ChatMessageViewData>()
    private val mAdapter = SuperAdapter(mMessage)
    private var mShowEmoji = false

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
        mEtMessage.clearFocus()
        initMultiMessageClickListener()

        mAdapter.addViewHolderForType(
            ChatMessageViewData::class.java,
            ChatMessageViewHolder::class.java
        )

        mRvMessages.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        mRvMessages.isNestedScrollingEnabled = false
        mRvMessages.layoutManager = GridLayoutManager(this, 1, RecyclerView.VERTICAL, true)
        mRvMessages.adapter = mAdapter

        mBtEmoji.setOnClickListener {
            showEmojiPopup()
        }
        mEtMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && mFlMultiMsg.isVisible) {
                mFlMultiMsg.gone()
            }
        }
        mBtMenu.setOnClickListener {
            onMenuClicked()
        }
        mBtAdd.setOnClickListener {
            showMultiMessage()
        }
        mBtSend.setOnClickListener {
            sendMessage()
        }
        KeyboardUtils.registerSoftInputChangedListener(this.window) {
            KeyboardUtils.unregisterSoftInputChangedListener(this.window)
            if (it == 0) {
                return@registerSoftInputChangedListener
            }
            mEmojiPopup.setPopupWindowHeight(it)
        }

        if (mSession.type != Constants.SESSION_TYPE_GROUP) {
            mBtMenu.gone()
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

    private fun onMenuClicked() {
        val popupMenu = PopupMenu(this, mBtMenu)
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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (event.y < mRvMessages.y + mRvMessages.measuredHeight) {
                onNonEditAreaClicked()
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun onNonEditAreaClicked() {
        KeyboardUtils.hideSoftInput(this)
        if (mShowEmoji) {
            mEmojiPopup.dismiss()
            mShowEmoji = false
            mBtEmoji.setIconResource(R.drawable.ic_emoji)
        }
        if (mFlMultiMsg.isVisible) {
            mFlMultiMsg.gone()
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

    private fun showEmojiPopup() {
        if (mFlMultiMsg.isVisible) {
            mFlMultiMsg.gone()
        }
        mEmojiPopup.toggle()
        mBtEmoji.setIconResource(if (mShowEmoji) R.drawable.ic_emoji else R.drawable.ic_keyboard)
        mShowEmoji = !mShowEmoji
    }

    private fun showMultiMessage() {
        mEtMessage.clearFocus()
        KeyboardUtils.hideSoftInput(this)
        if (mShowEmoji) {
            mEmojiPopup.dismiss()
            mShowEmoji = false
        }
        if (mFlMultiMsg.isVisible) {
            return
        }
        mFlMultiMsg.show()
    }

    private fun initMultiMessageClickListener() {
        val onClick = { _: View ->
            toast("TODO")
        }
        mBtImage.setOnClickListener(onClick)
        mBtCamera.setOnClickListener(onClick)
        mBtVoice.setOnClickListener(onClick)
        mBtFile.setOnClickListener(onClick)
        mBtVote.setOnClickListener(onClick)
        mBtLocation.setOnClickListener(onClick)
    }
}