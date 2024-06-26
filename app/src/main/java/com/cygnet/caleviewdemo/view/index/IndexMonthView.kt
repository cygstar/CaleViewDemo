package com.cygnet.caleviewdemo.view.index

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.MonthView

/**
 * 下标标记的日历控件
 * Created by huanghaibin on 2017/11/15.
 */
class IndexMonthView(context: Context) : MonthView(context) {

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

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        mSelectedPaint.style = Paint.Style.FILL
        canvas.drawRect((x + mPadding).toFloat(), (y + mPadding).toFloat(), (x + mItemWidth - mPadding).toFloat(), (y + mItemHeight - mPadding).toFloat(), mSelectedPaint)
        return true
    }

    /**
     * onDrawSelected
     *
     * @param canvas   canvas
     * @param calendar 日历calendar
     * @param x        日历Card x起点坐标
     * @param y        日历Card y起点坐标
     */
    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        mSchemeBasicPaint.color = calendar.schemeColor
        canvas.drawRect(
            (x + mItemWidth / 2 - mW / 2).toFloat(),
            (y + mItemHeight - mH * 2 - mPadding).toFloat(),
            (x + mItemWidth / 2 + mW / 2).toFloat(),
            (y + mItemHeight - mH - mPadding).toFloat(), mSchemeBasicPaint
        )
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val cx = x + mItemWidth / 2
        val top = y - mItemHeight / 6
        if (hasScheme) {
            canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint)
            canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10, if (calendar.isCurrentDay) mCurDayLunarTextPaint else mCurMonthLunarTextPaint)
        }
        else {
            canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mCurMonthTextPaint else mOtherMonthTextPaint)
            canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10, mCurMonthLunarTextPaint)
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
