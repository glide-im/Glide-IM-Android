package pro.glideim.ui.profile

import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.UserPerf
import pro.glideim.base.BaseFragment
import pro.glideim.sdk.GlideIM
import pro.glideim.ui.LoginActivity
import pro.glideim.utils.loadImage

class ProfileFragment : BaseFragment() {

    private val mBtLogout by lazy { findViewById<MaterialButton>(R.id.bt_logout) }
    private val mTvUid by lazy { findViewById<MaterialTextView>(R.id.tv_uid) }
    private val mTvNickname by lazy { findViewById<MaterialTextView>(R.id.tv_nickname) }
    private val mIvAvatar by lazy { findViewById<ImageView>(R.id.iv_avatar) }

    override val layoutRes = R.layout.fragment_profile

    override fun initView() {

        mBtLogout.setOnClickListener {
            UserPerf.logout()
            activity?.finish()
            LoginActivity.start(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        GlideIM.getInstance().account.profile.apply {
            mIvAvatar.loadImage(avatar)
            mTvUid.text = "uid: ${GlideIM.getInstance().myUID}"
            mTvNickname.text = nickname
        }
    }
}