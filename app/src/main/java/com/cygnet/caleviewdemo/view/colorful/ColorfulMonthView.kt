package com.cygnet.caleviewdemo.view.colorful

import android.content.Context
import android.graphics.Canvas
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.MonthView
import kotlin.math.min

/**
 * 高仿魅族日历布局
 * Created by huanghaibin on 2017/11/15.
 */
class ColorfulMonthView(context: Context) : MonthView(context) {

    private var mRadius = 0

    override fun onPreviewHook() {
        mRadius = (min(mItemWidth.toDouble(), mItemHeight.toDouble()) / 5 * 2).toInt()
    }

    /**
     * 如果需要点击Scheme没有效果，则return true
     *
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return false 则不绘制onDrawScheme，因为这里背景色是互斥的
     */
    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSchemePaint)
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val cx = x + mItemWidth / 2
        val top = y - mItemHeight / 8
        when {
            isSelected -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, if (calendar.isCurrentDay) mCurDayTextPaint else mSelectTextPaint)
                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10, if (calendar.isCurrentDay) mCurDayLunarTextPaint else mSelectedLunarTextPaint)
            }

            hasScheme -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint)
                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10, mSchemeLunarTextPaint)
            }

            else -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mCurMonthTextPaint else mOtherMonthTextPaint)
                canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10, if (calendar.isCurrentDay) mCurDayLunarTextPaint else mCurMonthLunarTextPaint)
            }
        }
    }
}
