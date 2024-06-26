package com.cygnet.caleviewdemo.view.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.WeekView
import kotlin.math.min

/**
 * 精美进度风格
 * Created by huanghaibin on 2018/2/8.
 */
class ProgressWeekView(context: Context) : WeekView(context) {

    private val mNoneProgressPaint = Paint()
    private val mProgressPaint = Paint()
    private var mRadius = 0

    init {
        mProgressPaint.isAntiAlias = true
        mProgressPaint.style = Paint.Style.STROKE
        mProgressPaint.strokeWidth = dipToPx(context, 2.2f).toFloat()
        mProgressPaint.color = -0x440ab600

        mNoneProgressPaint.isAntiAlias = true
        mNoneProgressPaint.style = Paint.Style.STROKE
        mNoneProgressPaint.strokeWidth = dipToPx(context, 2.2f).toFloat()
        mNoneProgressPaint.color = -0x6f303031
    }

    override fun onPreviewHook() {
        mRadius = (min(mItemWidth.toDouble(), mItemHeight.toDouble()) / 11 * 4).toInt()
    }

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean): Boolean {
        val cx = x + mItemWidth / 2
        val cy = mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
        return false
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int) {
        val cx = x + mItemWidth / 2
        val cy = mItemHeight / 2

        val angle = getAngle(calendar.scheme!!.toInt())

        val progressRectF = RectF((cx - mRadius).toFloat(), (cy - mRadius).toFloat(), (cx + mRadius).toFloat(), (cy + mRadius).toFloat())
        canvas.drawArc(progressRectF, -90f, angle.toFloat(), false, mProgressPaint)

        val noneRectF = RectF((cx - mRadius).toFloat(), (cy - mRadius).toFloat(), (cx + mRadius).toFloat(), (cy + mRadius).toFloat())
        canvas.drawArc(noneRectF, (angle - 90).toFloat(), (360 - angle).toFloat(), false, mNoneProgressPaint)
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean, isSelected: Boolean) {
        val baselineY = mTextBaseLine
        val cx = x + mItemWidth / 2
        when {
            isSelected -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, mSelectTextPaint)
            hasScheme -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint)
            else -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mCurMonthTextPaint else mOtherMonthTextPaint)
        }
    }

    companion object {
        /**
         * 获取角度
         *
         * @param progress 进度
         * @return 获取角度
         */
        private fun getAngle(progress: Int): Int {
            return (progress * 3.6).toInt()
        }

        /**
         * dp转px
         *
         * @param context context
         * @param dp dp
         * @return px
         */
        private fun dipToPx(context: Context, dp: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }
    }
}
