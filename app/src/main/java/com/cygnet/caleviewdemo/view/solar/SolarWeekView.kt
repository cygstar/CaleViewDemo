package com.cygnet.caleviewdemo.view.solar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.WeekView
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 精致周视图,using trigonometric function
 * Created by huanghaibin on 2018/2/8.
 */
class SolarWeekView(context: Context) : WeekView(context) {

    private val mPointPaint = Paint()

    private var mRadius = 0
    private val mPointRadius: Int

    init {
        mPointPaint.isAntiAlias = true
        mPointPaint.style = Paint.Style.FILL
        mSchemePaint.style = Paint.Style.STROKE
        mSchemePaint.strokeWidth = dipToPx(context, 1.2f).toFloat()
        mSchemePaint.color = -0x1
        mPointRadius = dipToPx(context, 3.6f)
        mPointPaint.color = Color.RED
    }

    override fun onPreviewHook() {
        mRadius = (min(mItemWidth.toDouble(), mItemHeight.toDouble()) / 5 * 2).toInt()
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
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSchemePaint)

        val schemes: List<Calendar.Scheme>? = calendar.schemes

        mPointPaint.color = schemes!![0].schemeColor //You can also use three fixed Paint 你也可以使用三个Paint对象
        val rightTopX = (cx + mRadius * cos(-10 * Math.PI / 180)).toInt()
        val rightTopY = (cy + mRadius * sin(-10 * Math.PI / 180)).toInt()
        canvas.drawCircle(rightTopX.toFloat(), rightTopY.toFloat(), mPointRadius.toFloat(), mPointPaint)

        mPointPaint.color = schemes[1].schemeColor
        val leftTopX = (cx + mRadius * cos(-140 * Math.PI / 180)).toInt()
        val leftTopY = (cy + mRadius * sin(-140 * Math.PI / 180)).toInt()
        canvas.drawCircle(leftTopX.toFloat(), leftTopY.toFloat(), mPointRadius.toFloat(), mPointPaint)

        mPointPaint.color = schemes[2].schemeColor
        val bottomX = (cx + mRadius * cos(100 * Math.PI / 180)).toInt()
        val bottomY = (cy + mRadius * sin(100 * Math.PI / 180)).toInt()
        canvas.drawCircle(bottomX.toFloat(), bottomY.toFloat(), mPointRadius.toFloat(), mPointPaint)
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean, isSelected: Boolean) {
        val baselineY = mTextBaseLine
        val cx = x + mItemWidth / 2
        when {
            isSelected -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, mSelectTextPaint)
            hasScheme -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mSchemeTextPaint else mSchemeTextPaint)
            else -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mCurMonthTextPaint else mCurMonthTextPaint)
        }
    }

    companion object {
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
