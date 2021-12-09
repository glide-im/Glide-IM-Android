package pro.glideim.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.dengzii.ktx.android.dp2px
import pro.glideim.R
import kotlin.math.roundToInt

class LetterTouchView : View {

    private val mLetters = mutableListOf("A", "B", "C", "D")
    private val mLetterHeight = 20f.dp2px()
    private val mPaddingTop = 8f.dp2px()
    private val mPaddingBottom = 4f.dp2px()
    private val mLetterSize = resources.getDimension(16f.dp2px())
    private val mBackgroundColor = resources.getColor(R.color.primaryLightColor, null)
    private val mTextColor = resources.getColor(R.color.white, null)
    private val mTextBounds = Rect()

    private var mLatestSelect = -1

    private var mOnSelectListener: ((y: Float, index: Int, letter: String) -> Unit)? =
        null

    private val mPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            textSize = mLetterSize
        }
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                var index = ((event.y - mPaddingTop) / mLetterHeight).roundToInt()
                if (index >= mLetters.size) {
                    index = mLetters.size - 1
                }
                if (index < 0) {
                    index = 0
                }
                if (mLatestSelect != index) {
                    mLatestSelect = index
                    onLetterSelected(event.x, event.y, index)
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint.color = mBackgroundColor
        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            10f.dp2px().toFloat(),
            10f.dp2px().toFloat(),
            mPaint
        )
        val textHeight = if (mLetters.isNotEmpty()) {
            mPaint.getTextBounds(mLetters[0], 0, 0, mTextBounds)
            mTextBounds.height()
        } else {
            0
        }
        mPaint.color = mTextColor
        mLetters.forEachIndexed { index, s ->
            val y = index * mLetterHeight + mPaddingTop + mLetterHeight / 2f - textHeight / 2f
            val x = measuredWidth / 2f - mPaint.measureText(s) / 2f
            canvas.drawText(s, x, y, mPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val h = if (mLetters.isEmpty()) {
            0
        } else {
            mLetterHeight * mLetters.size + mPaddingTop + mPaddingBottom
        }
        setMeasuredDimension(22f.dp2px(), h)
    }

    fun setLetters(letter: List<String>) {
        mLetters.clear()
        mLetters.addAll(letter)
        if (isActivated && isShown) {
            requestLayout()
            invalidate()
        }
    }

    fun setOnLetterSelectListener(l: (y: Float, index: Int, letter: String) -> Unit) {
        mOnSelectListener = l
    }

    private fun onLetterSelected(x: Float, y: Float, index: Int) {
        val ry = index * mLetterHeight + mPaddingTop + mLetterHeight / 2f
        mOnSelectListener?.invoke(ry, index, mLetters[index])
    }
}