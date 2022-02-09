package pro.glideim.utils

import android.content.Context
import android.graphics.*
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dengzii.ktx.android.dp2px
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import pro.glideim.R
import pro.glideim.sdk.GlideIM
import pro.glideim.sdk.IMAccount
import pro.glideim.sdk.api.user.UserInfoBean

object GroupAvatarUtils {

    private const val TAG = "GroupAvatarUtils"

    fun loadAvatar(account: IMAccount?, gid: Long, radius: Float, imageView: ImageView) {

        val context = imageView.context
        val c = account?.contactsList?.getGroup(gid) ?: return

        val len = c.members.size
        val sub = c.members.subList(0, if (len > 9) 9 else len)

        // TODO 2022-2-8 18:55:22 temp bitmap
        val bitmaps = GlideIM.getUserInfo(sub.map { it.uid })
            .flatMapObservable { Observable.fromIterable(it) }
            .flatMap { getUserAvatar(context, it) }
            .toList()
            .flatMap { Single.just(combineAvatar(it, Color.valueOf(Color.LTGRAY))) }
            .io2main()

        bitmaps.subscribe(object : SingleObserver<Bitmap> {
            override fun onSubscribe(d: Disposable) {
                (context as? LifecycleOwner)?.onState(Lifecycle.State.DESTROYED) {
                    d.dispose()
                }
            }

            override fun onSuccess(t: Bitmap) {
                Glide.with(imageView)
                    .load(t)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(radius.dp2px())))
                    .into(imageView)
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "onError: load group avatar", e)
            }
        })
    }

    private fun getUserAvatar(context: Context, ui: UserInfoBean): Observable<Bitmap> {
        return Observable.create { emitter ->
            val avatar = loadBitmapFromUrl(context, ui.avatar)
            emitter.onNext(avatar)
            emitter.onComplete()
        }
    }

    private fun combineAvatar(avatars: List<Bitmap>, bgColor: Color): Bitmap {

        val config = Bitmap.Config.RGB_565
        val combine = Bitmap.createBitmap(100, 100, config, true)
        val canvas = Canvas(combine)
        if (avatars.isEmpty()) {
            return combine
        }

        val h = 28f
        val paint: Paint? = null

        val margin = 4
        val lineHeight = h + margin / 2

        val combineLine: (c: Int, b: List<Bitmap>) -> Bitmap = { c, b ->
            val lineWith = h.toInt() * c + margin * (c - 1)
            val lineBitmap = Bitmap.createBitmap(lineWith, lineHeight.toInt(), config, true)
            val lineCanvas = Canvas(lineBitmap)
            lineCanvas.drawColor(bgColor.toArgb())
            b.forEachIndexed { index, bitmap ->
                val left = (margin + h) * index
                lineCanvas.drawBitmap(bitmap, left, 0f, paint)
            }
            lineBitmap
        }

        val lines = mutableListOf<Bitmap>()
        val tempLines = mutableListOf<Bitmap>()
        val lineAvatars = if (avatars.size <= 4) 2 else 3
        avatars.forEach {
            if (tempLines.size >= lineAvatars) {
                val line = combineLine(lineAvatars, tempLines)
                lines.add(line)
                tempLines.clear()
            }
            tempLines.add(it)
        }
        if (tempLines.size != 0) {
            val line = combineLine(tempLines.size, tempLines)
            lines.add(line)
        }

        val allLineHeight = lineHeight.toInt() * lines.size + margin * (lines.size - 1)
        val allLineBitmap = Bitmap.createBitmap(100, allLineHeight, config, true)
        val allLinesCanvas = Canvas(allLineBitmap)
        allLinesCanvas.drawColor(bgColor.toArgb())
        lines.forEachIndexed { i, d ->
            val left = 100 / 2 - d.width / 2f
            val top = i * lineHeight + margin * i
            allLinesCanvas.drawBitmap(d, left, top, paint)
        }

        canvas.drawColor(bgColor.toArgb())
        canvas.drawBitmap(
            allLineBitmap,
            100f / 2 - allLineBitmap.width / 2,
            100f / 2 - allLineBitmap.height / 2,
            paint
        )

        return combine
    }

    private fun loadBitmapFromUrl(context: Context, url: String): Bitmap {
        return try {
            Glide.with(context)
                .load(url)
                .error(R.mipmap.ic_launcher)
                .submit(30, 30)
                .get()
                .toBitmap()
        } catch (e: Exception) {
            return BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        }
    }
}