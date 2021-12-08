package pro.glideim.ui.chat

import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dengzii.adapter.SuperAdapter
import com.dengzii.adapter.addViewHolderForType
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pro.glideim.R
import pro.glideim.base.BaseActivity

class ChatActivity : BaseActivity() {

    private val mBtSend by lazy { findViewById<MaterialButton>(R.id.bt_send) }
    private val mEtMessage by lazy { findViewById<TextInputEditText>(R.id.et_message) }
    private val mRvMessages by lazy { findViewById<RecyclerView>(R.id.rv_messages) }

    private val mMessage = mutableListOf<String>()
    private val mAdapter = SuperAdapter(mMessage)

    override val layoutResId = R.layout.activity_chat

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, ChatActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun initView() {

        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mMessage.add("")
        mAdapter.addViewHolderForType<String>(R.layout.item_chat_message) {
            onBindData { _, position ->
                findView<TextView>(R.id.tv_msg).text = "this is message position = $position"
            }
        }
        mRvMessages.adapter = mAdapter

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, true)
        layoutManager.stackFromEnd = true
        mRvMessages.layoutManager = layoutManager
    }
}