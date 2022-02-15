package pro.glideim

import android.content.Context
import com.dengzii.ktx.android.content.Preferences
import com.dengzii.ktx.android.content.preference

class UserConfig(context: Context) : Preferences(context, "user_config") {
    var password by preference("")
    var account by preference("")
    var lastUpdateCheck by preference("0")
    var newestVersion by preference(0)
}