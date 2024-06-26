package com.cygnet.caleviewdemo.view.index

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.WeekView

/**
 * 下标周视图
 * Created by huanghaibin on 2017/11/29.
 */
class IndexWeekView(context: Context) : WeekView(context) {

    private val mSchemeBasicPaint = Paint()
    private val mPadding: Int
    private val mH: Int
    private val mW: Int

    init {
        mSchemeBasicPaint.isAntiAlias = true
        mSchemeBasicPaint.style = Paint.Style.FILL
        mSchemeBasicPaint.textAlign = Paint.Align.CENTER
        mSchemeBasicPaint.color = -0xcccccd
        mSchemeBasicPaint.isFakeBoldText = true
        mPadding = dipToPx(getContext(), 4f)
        mH = dipToPx(getContext(), 2f)
        mW = dipToPx(getContext(), 8f)
    }

    override fun onPreviewHook() {}

    /**
     * 如果这里和 onDrawScheme 是互斥的，则 return false，
     * return true 会先绘制 onDrawSelected，再绘制onDrawSelected
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param hasScheme hasScheme 非标记的日期
     */
    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean): Boolean {
        mSelectedPaint.style = Paint.Style.FILL
        canvas.drawRect((x + mPadding).toFloat(), mPadding.toFloat(), (x + mItemWidth - mPadding).toFloat(), (mItemHeight - mPadding).toFloat(), mSelectedPaint)
        return true
    }

    /**
     * 绘制下标标记
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     */
    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int) {
        mSchemeBasicPaint.color = calendar.schemeColor
        canvas.drawRect(
            (x + mItemWidth / 2 - mW / 2).toFloat(),
            (mItemHeight - mH * 2 - mPadding).toFloat(),
            (x + mItemWidth / 2 + mW / 2).toFloat(),
            (mItemHeight - mH - mPadding).toFloat(), mSchemeBasicPaint
        )
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean, isSelected: Boolean) {
        val cx = x + mItemWidth / 2
        val top = -mItemHeight / 6
        if (hasScheme) {
            canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mSchemeTextPaint else mCurMonthTextPaint)
            canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + mItemHeight / 10, if (calendar.isCurrentDay) mCurDayLunarTextPaint else mCurMonthLunarTextPaint)
        }
        else {
            canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mCurMonthTextPaint else mCurMonthTextPaint)
            canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + mItemHeight / 10, if (calendar.isCurrentDay) mCurDayLunarTextPaint else mCurMonthLunarTextPaint)
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
