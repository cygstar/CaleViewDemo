package com.cygnet.caleviewdemo.view.meizu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.WeekView

/**
 * 魅族周视图
 * Created by huanghaibin on 2017/11/29.
 */
class MeizuWeekView(context: Context) : WeekView(context) {

    private val mTextPaint = Paint()

    private val mSchemeBasicPaint = Paint()
    private val mRadio: Float
    private val mPadding: Int
    private val mSchemeBaseLine: Float

    init {
        mTextPaint.textSize = dipToPx(context, 8f).toFloat()
        mTextPaint.color = -0x1
        mTextPaint.isAntiAlias = true
        mTextPaint.isFakeBoldText = true

        mSchemeBasicPaint.isAntiAlias = true
        mSchemeBasicPaint.style = Paint.Style.FILL
        mSchemeBasicPaint.textAlign = Paint.Align.CENTER
        mSchemeBasicPaint.color = -0x12acad
        mSchemeBasicPaint.isFakeBoldText = true
        mRadio = dipToPx(getContext(), 7f).toFloat()
        mPadding = dipToPx(getContext(), 4f)
        val metrics = mSchemeBasicPaint.fontMetrics
        mSchemeBaseLine = mRadio - metrics.descent + (metrics.bottom - metrics.top) / 2 + dipToPx(getContext(), 1f)
    }

    /**
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean): Boolean {
        mSelectedPaint.style = Paint.Style.FILL
        canvas.drawRect((x + mPadding).toFloat(), mPadding.toFloat(), (x + mItemWidth - mPadding).toFloat(), (mItemHeight - mPadding).toFloat(), mSelectedPaint)
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int) {
        mSchemeBasicPaint.color = calendar.schemeColor
        canvas.drawCircle(x + mItemWidth - mPadding - mRadio / 2, mPadding + mRadio, mRadio, mSchemeBasicPaint)
        canvas.drawText(calendar.scheme!!, x + mItemWidth - mPadding - mRadio / 2 - getTextWidth(calendar.scheme) / 2, mPadding + mSchemeBaseLine, mTextPaint)
    }

    private fun getTextWidth(text: String?): Float {
        return mTextPaint.measureText(text)
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean, isSelected: Boolean) {
        val cx = x + mItemWidth / 2
        val top = -mItemHeight / 6
        val isInRange = isInRange(calendar)
        when {
            isSelected -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, mSelectTextPaint)
                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + mItemHeight / 10, mSelectedLunarTextPaint)
            }
            hasScheme -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, if (calendar.isCurrentMonth && isInRange) mSchemeTextPaint else mOtherMonthTextPaint)
                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + mItemHeight / 10, mCurMonthLunarTextPaint)
            }
            else -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, if (calendar.isCurrentDay && isInRange) mCurDayTextPaint else if (calendar.isCurrentMonth && isInRange) mCurMonthTextPaint else mOtherMonthTextPaint)
                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + mItemHeight / 10, if (calendar.isCurrentDay && isInRange) mCurDayLunarTextPaint else if (calendar.isCurrentMonth) mCurMonthLunarTextPaint else mOtherMonthLunarTextPaint)
            }
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
