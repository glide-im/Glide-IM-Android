package pro.glideim.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.dengzii.ktx.format
import pro.glideim.BuildConfig
import pro.glideim.sdk.api.app.ReleaseInfoBean
import java.util.*

object UpdateUtils {

    fun showUpdate(context: Context, info: ReleaseInfoBean, callback: (cancel: Boolean) -> Unit) {
        val builder = AlertDialog.Builder(context)
        val date = Date(info.updateAt * 1000)
        val format = date.format("yyyy MM.dd")
        builder.setCancelable(false)
        builder.setTitle("New Version ${info.versionName}")
        builder.setMessage(
            "Current: ${BuildConfig.VERSION_NAME}\n" +
                    "Date: $format\n" +
                    "What's New: \n" +
                    "${info.description}\n"
        )
        builder.setPositiveButton("Download") { d, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(info.downloadUrl)
            context.startActivity(intent)
            d.dismiss()
            callback(false)
        }
        builder.setNegativeButton("Cancel") { d, _ ->
            d.dismiss()
            callback(true)
        }
        builder.create().show()
    }
}