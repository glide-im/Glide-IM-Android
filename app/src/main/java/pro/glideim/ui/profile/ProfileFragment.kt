package pro.glideim.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.ImageView
import com.dengzii.ktx.android.antiShakeClick
import com.dengzii.ktx.android.content.update
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import io.reactivex.Single
import pro.glideim.BuildConfig
import pro.glideim.R
import pro.glideim.UserConfig
import pro.glideim.base.BaseFragment
import pro.glideim.db.GlideIMDatabase
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.api.app.AppApi
import pro.glideim.sdk.utils.RxUtils
import pro.glideim.ui.LoginActivity
import pro.glideim.ui.MainActivity
import pro.glideim.utils.UpdateUtils
import pro.glideim.utils.loadImageRoundCorners
import pro.glideim.utils.request2


class ProfileFragment : BaseFragment() {

    private val mBtCleanCache by lazy { findViewById<MaterialButton>(R.id.bt_clean_cache) }
    private val mBtEditProfile by lazy { findViewById<MaterialButton>(R.id.bt_update_profile) }
    private val mBtSettings by lazy { findViewById<MaterialButton>(R.id.bt_setting) }
    private val mBtUpdate by lazy { findViewById<MaterialButton>(R.id.bt_update) }

    private val mBtLogout by lazy { findViewById<MaterialButton>(R.id.bt_logout) }
    private val mBtUid by lazy { findViewById<MaterialButton>(R.id.bt_uid) }
    private val mTvNickname by lazy { findViewById<MaterialTextView>(R.id.tv_nickname) }
    private val mIvAvatar by lazy { findViewById<ImageView>(R.id.iv_avatar) }

    override val layoutRes = R.layout.fragment_profile

    override fun initView() {

        mBtLogout.antiShakeClick {
            GlideIM.getAccount().logout()
            LoginActivity.start(requireContext())
            activity?.finish()
        }

        mBtUpdate.text = "Check for Update (${BuildConfig.VERSION_NAME})"
        mBtUpdate.antiShakeClick {
            AppApi.API.releaseInfo
                .map(RxUtils.bodyConverter())
                .request2(this) {
                    UserConfig(requireContext()).update {
                        lastUpdateCheck = (System.currentTimeMillis() / 1000).toString()
                    }
                    if (it.versionCode > BuildConfig.VERSION_CODE) {
                        UpdateUtils.showUpdate(requireContext(), it) {}
                    } else {
                        toast("No new version")
                    }
                }
        }
        mBtCleanCache.antiShakeClick {
            Single.create<Boolean> {
                GlideIMDatabase.getDb(requireContext()).clearAllTables()
                it.onSuccess(true)
            }.request2(requireActivity() as MainActivity) {
                toast("Complete!")
            }
        }
        mBtEditProfile.antiShakeClick {
            toast("TODO")
        }
        mBtSettings.antiShakeClick {
            toast("TODO")
        }
        mIvAvatar.antiShakeClick {
            toast("TODO")
        }
        mTvNickname.antiShakeClick {
            toast("TODO")
        }
    }

    override fun onResume() {
        super.onResume()
        GlideIM.getAccount().profile?.apply {
            mIvAvatar.loadImageRoundCorners(avatar, 14f)
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