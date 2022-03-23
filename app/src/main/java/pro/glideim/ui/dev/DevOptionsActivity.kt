package pro.glideim.ui.dev

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.View
import androidx.core.content.edit
import com.blankj.utilcode.util.AppUtils
import com.dengzii.ktx.android.content.update
import com.dengzii.ktx.android.content.use
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import pro.glideim.BuildConfig
import pro.glideim.R
import pro.glideim.UserConfig
import pro.glideim.base.BaseActivity

class DevOptionsActivity : BaseActivity() {

    private val mSwEnableCache by lazy { findViewById<SwitchMaterial>(R.id.sw_enable_cache) }
    private val mEtBaseUrl by lazy { findViewById<TextInputEditText>(R.id.et_base_url) }

    override val layoutResId = R.layout.activity_dev_options

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, DevOptionsActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        needAuth = false
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        title = "Developer Options"

        UserConfig(this).use {
            mSwEnableCache.isChecked = enableCache
            mEtBaseUrl.setText(baseUrl)
        }
    }

    private fun apply() {
        UserConfig(this).update {
            enableCache = mSwEnableCache.isChecked
            baseUrl = mEtBaseUrl.text.toString()
        }
        AppUtils.relaunchApp(true)
    }

    private fun reset() {
        mSwEnableCache.isChecked = false
        mEtBaseUrl.setText(BuildConfig.BASE_URL)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu ?: return super.onCreateOptionsMenu(menu)

        val reset = menu.add(0, 1, 0, "Reset")
        reset.setShowAsAction(1)
        reset.isVisible = true
        reset.setOnMenuItemClickListener {
            reset()
            false
        }

        val apply = menu.add(0, 2, 0, "Apply")
        apply.setShowAsAction(2)
        apply.isVisible = true
        apply.setOnMenuItemClickListener {
            apply()
            false
        }

        return true
    }
}