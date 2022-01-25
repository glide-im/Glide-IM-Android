package pro.glideim.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import pro.glideim.R
import pro.glideim.base.BaseFragment
import pro.glideim.sdk.GlideIM
import pro.glideim.ui.LoginActivity
import pro.glideim.utils.loadImage


class ProfileFragment : BaseFragment() {

    private val mLlCleanCache by lazy { findViewById<LinearLayoutCompat>(R.id.ll_clean_cache) }
    private val mLlEditProfile by lazy { findViewById<LinearLayoutCompat>(R.id.ll_edit_profile) }
    private val mLlSettings by lazy { findViewById<LinearLayoutCompat>(R.id.ll_settings) }

    private val mBtLogout by lazy { findViewById<MaterialButton>(R.id.bt_logout) }
    private val mBtUid by lazy { findViewById<MaterialButton>(R.id.bt_uid) }
    private val mTvNickname by lazy { findViewById<MaterialTextView>(R.id.tv_nickname) }
    private val mIvAvatar by lazy { findViewById<ImageView>(R.id.iv_avatar) }

    override val layoutRes = R.layout.fragment_profile

    override fun initView() {

        mBtLogout.setOnClickListener {
            GlideIM.getAccount().logout()
            activity?.finish()
            LoginActivity.start(requireContext())
        }

        mLlCleanCache.setOnClickListener {
            toast("TODO")
        }
        mLlEditProfile.setOnClickListener {
            toast("TODO")
        }
        mLlSettings.setOnClickListener {
            toast("TODO")
        }
    }

    override fun onResume() {
        super.onResume()
        GlideIM.getAccount().profile?.apply {
            mIvAvatar.loadImage(avatar)
            mBtUid.text = "uid: $uid"
            mBtUid.setOnClickListener {
                val cm: ClipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val d = ClipData.newPlainText("glide-im uid", uid.toString())
                cm.setPrimaryClip(d)
                toast("Copied")
            }
            mTvNickname.text = nickname
        }
    }
}