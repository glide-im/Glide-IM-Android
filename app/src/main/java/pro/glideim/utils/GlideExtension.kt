package pro.glideim.utils

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import pro.glideim.R

fun ImageView.loadImage(@DrawableRes res: Int) {

    Glide.with(this)
        .load(res)
        .apply(RequestOptions.fitCenterTransform())
        .into(this).clearOnDetach()
}


fun ImageView.loadImage(url: String?) {

    if (url.isNullOrEmpty()) {
        loadImage(R.mipmap.ic_launcher)
        return
    }
    Glide.with(this)
        .load(url)
        .apply(RequestOptions.fitCenterTransform())
        .placeholder(R.mipmap.ic_launcher)
        .into(this)
        .clearOnDetach()
}


fun ImageView.loadImageClipCircle(url: String?) {

    if (url.isNullOrEmpty()) {
        return
    }
    Glide.with(this)
        .load(url)
        .apply(RequestOptions.bitmapTransform(CircleCrop()))
        .placeholder(R.mipmap.ic_launcher)
        .into(this)
        .clearOnDetach()
}