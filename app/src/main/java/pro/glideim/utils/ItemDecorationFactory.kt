package pro.glideim.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dengzii.ktx.android.dp2px


object ItemDecorationFactory {

    fun createOffset(
        dp: Int
    ): RecyclerView.ItemDecoration {

        return object : RecyclerView.ItemDecoration() {
            val size = dp.toFloat().dp2px()
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(0, size / 2, 0, size / 2)
            }
        }
    }

    fun createDivider(
        dpSize: Float, color: Int,
        marginStart: Float = 0f,
        marginEnd: Float = 0f
    ): RecyclerView.ItemDecoration {

        return object : RecyclerView.ItemDecoration() {
            val size = dpSize.dp2px()
            val paint by lazy {
                Paint().apply {
                    setColor(color)
                    strokeWidth = size.toFloat()
                }
            }
            val startX = marginStart.dp2px().toFloat()

            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(0, size / 2, 0, size / 2)
            }

            override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                super.onDrawOver(c, parent, state)
                for (i in 0 until parent.childCount - 1) {
                    val view = parent.getChildAt(i)
                    val top = view.bottom.toFloat()
//                    val left = view.paddingLeft + size
                    val right = (view.width - view.paddingRight - size).toFloat()
//                    val bottom = top + size
                    c.drawLine(startX, top, right - marginEnd.dp2px(), top, paint)
                }
            }
        }
    }

}