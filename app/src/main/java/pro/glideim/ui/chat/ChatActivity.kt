package pro.glideim.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.children
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.vanniktech.emoji.EmojiPopup
import pro.glideim.R
import pro.glideim.base.BaseActivity
import pro.glideim.sdk.*
import pro.glideim.sdk.api.group.AddGroupMemberDto
import pro.glideim.sdk.api.group.GroupApi
import pro.glideim.sdk.messages.ChatMessage
import pro.glideim.sdk.messages.GroupNotify
import pro.glideim.ui.SortedList
import pro.glideim.ui.chat.viewholder.*
import pro.glideim.ui.contacts.SelectContactsActivity
import pro.glideim.ui.group.GroupDetailActivity
import pro.glideim.utils.convert
import pro.glideim.utils.io2main
import pro.glideim.utils.request
import pro.glideim.utils.request2

open class ChatActivity : BaseActivity(), IMSession.SessionUpdateListener {

    private val mBtSend by lazy { findViewById<MaterialButton>(R.id.bt_send) }
    private val mEtMessage by lazy { findViewById<TextInputEditText>(R.id.et_message) }
    private val mRvMessages by lazy { findViewById<RecyclerView>(R.id.rv_messages) }
    private val mTvTitle by lazy { findViewById<MaterialTextView>(R.id.tv_title) }
    private val mBtMenu by lazy { findViewById<MaterialButton>(R.id.bt_more) }
    private val mBtEmoji by lazy { findViewById<MaterialButton>(R.id.bt_emoji) }
    private val mBtAdd by lazy { findViewById<MaterialButton>(R.id.bt_add) }
    private val mFlMultiMsg by lazy { findViewById<ViewGroup>(R.id.fl_multi_message) }
    private val mVgMessage by lazy { findViewById<ViewGroup>(R.id.cl_msg) }

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
        mSession = GlideIM.getAccount().imSessionList.getOrCreate(type, id)
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        mMessage.l = SortedList(
            ChatMessageViewData::class.java,
            MessageListSorter(mAdapter)
        )
        mEtMessage.toggleEnable()
        mBtSend.toggleEnable()
        mEtMessage.clearFocus()
        initMultiMessageClickListener()

        mAdapter.addViewHolderForType(
            ChatMessageViewData::class.java,
            ChatMessageViewHolder::class.java
        )
        mAdapter.addViewHolderForType(
            ChatImageMessageViewData::class.java,
            ChatImageMessageViewHolder::class.java
        )
        mAdapter.addViewHolderForType(
            NotifyViewData::class.java,
            GroupNotifyViewHolder::class.java
        )
        mAdapter.setOnItemLongClickListener { _, itemData, position ->
            onMessageLongClicked(itemData, position)
            return@setOnItemLongClickListener true
        }

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
            val msg = mEtMessage.text.toString()
            if (msg.isBlank()) {
                return@setOnClickListener
            }
            sendMessage(Constants.MESSAGE_TYPE_TEXT, msg)
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

    private fun sendMessage(type: Int, content: String) {
        if (!GlideIM.getAccount().isIMAvailable) {
            toast("IM server is disconnected")
            return
        }
        if (type == Constants.MESSAGE_TYPE_TEXT) {
            mEtMessage.setText("")
        }
        mSession.sendMessage(type, content)
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

    private fun onMessageLongClicked(m: Any, position: Int) {

        val mid = when (m) {
            is ChatImageMessageViewData -> m.message.mid
            is ChatMessageViewData -> m.message.mid
            else -> return
        }
        mSession.recallMessage(mid)
            .request2(this) {
                if (it.state == ChatMessage.STATE_SRV_FAILED) {
                    toast("recall message failed")
                }
                if (it.state == ChatMessage.STATE_SRV_RECEIVED){
                    toast("Recall message success")
                }
            }
    }

    override fun onResume() {
        super.onResume()
        NotificationUtils.cancel(mSession.to.hashCode())
        current = mSession
        mSession.addSessionUpdateListener(this)
        mSession.setMessageListener(object :
            MessageChangeListener {
            override fun onChange(mid: Long, message: IMMessage) {
                runOnUiThread {
                    addMessage(message)
                }
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
        mSession.removeSessionUpdateListener(this)
        mSession.setMessageListener(null)
    }

    private fun addMessage(m: IMMessage) {
        val viewData = if (m.status == IMMessage.STATUS_RECALLED) {
            val msg = if (m.recallBy == m.from) {
                if (m.from == account?.uid) "你撤回了一条消息" else "${m.title}撤回了一条消息"
            } else {
                "管理员撤回了一条消息"
            }
            Log.d(TAG, "addMessage: message recalled $m")
            val notifyViewData = NotifyViewData(m, msg)
            mMessage.add(notifyViewData)
            val indexOf = mMessage.indexOf(notifyViewData)
            mAdapter.notifyItemChanged(indexOf)
            return
        } else {
            when (m.type) {
                Constants.MESSAGE_TYPE_RECALL -> return
                Constants.MESSAGE_TYPE_TEXT -> ChatMessageViewData(true, m)
                Constants.MESSAGE_TYPE_IMAGE -> ChatImageMessageViewData(true, m)
                Constants.MESSAGE_TYPE_GROUP_NOTIFY -> {
                    val notify = (m as IMGroupNotifyMessage).notify
                    GlideIM.getUserInfo(notify.data.uid)
                        .request2(this) { ui ->
                            val nicknames = ui?.joinToString(",") { u -> u.nickname } ?: ""
                            val msg = when (notify.type.toInt()) {
                                GroupNotify.TYPE_MEMBER_ADDED -> "$nicknames 已加入群聊"
                                GroupNotify.TYPE_MEMBER_REMOVED -> "你已离开群聊"
                                else -> "-"
                            }
                            val vd = NotifyViewData(m, msg)
                            mMessage.add(vd)
                        }
                    return
                }
                else -> {
                    ChatMessageViewData(true, m, true)
                }
            }
        }
        mMessage.add(viewData)
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
                R.id.item_invite -> inviteMember()
                R.id.item_exit -> deleteContact()
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    private fun deleteContact() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Exist Group")
            setMessage("Exist?")
            setPositiveButton("Confirm") { d, w ->

            }
            setNegativeButton("Cancel") { d, w ->

            }
        }
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

    private fun hideMultiMessage() {
        if (mFlMultiMsg.isVisible) {
            mFlMultiMsg.gone()
        }
    }

    private fun initMultiMessageClickListener() {
        val onClick = { _: View ->
            toast("TODO")
            hideMultiMessage()
        }
        mBtImage.setOnClickListener {
            hideMultiMessage()
            sendMessage(
                Constants.MESSAGE_TYPE_IMAGE,
                "https://s2.loli.net/2022/01/25/YCJQ5k4mi9Sth8x.png"
            )
        }
        mBtCamera.setOnClickListener(onClick)
        mBtVoice.setOnClickListener(onClick)
        mBtFile.setOnClickListener(onClick)
        mBtVote.setOnClickListener(onClick)
        mBtLocation.setOnClickListener(onClick)
    }

    private fun inviteMember() {
        val group = account?.contactsList?.getGroup(mSession.to) ?: return
        val mb = group.members.map { it.uid }
        SelectContactsActivity.startForResult(
            this, "Invite Member",
            SelectContactsActivity.TYPE_USER, mb
        ) {
            if (it.isEmpty()) {
                return@startForResult
            }
            GroupApi.API.inviteMember(AddGroupMemberDto(mSession.to, it.toList()))
                .convert()
                .request2(this) {
                    toast("invite success!")
                }
        }
    }

    override fun onUpdate(s: IMSession) {
        if (s.existed) {
            mVgMessage.isClickable = false
            mVgMessage.isLongClickable = false
            mVgMessage.focusable = View.NOT_FOCUSABLE
            mVgMessage.isEnabled = false
            mVgMessage.children.forEach {
                it.isEnabled = false
            }
        }
    }
}