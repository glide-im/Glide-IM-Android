package pro.glideim.ui.profile

import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseFragment
import pro.glideim.ui.chat.ChatActivity

class ProfileFragment : BaseFragment() {

    private val mBtLogout by lazy { findViewById<MaterialButton>(R.id.bt_logout) }
    private val mTvUid by lazy { findViewById<MaterialTextView>(R.id.tv_uid) }
    private val mTvNickname by lazy { findViewById<MaterialTextView>(R.id.tv_nickname) }
    private val mIvAvatar by lazy { findViewById<ImageView>(R.id.iv_avatar) }

    override val layoutRes = R.layout.fragment_profile

    override fun initView() {

        mBtLogout.setOnClickListener {
            toast("TODO")
        }
    }
}