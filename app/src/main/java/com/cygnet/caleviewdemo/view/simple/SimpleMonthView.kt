package com.cygnet.caleviewdemo.view.simple

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.MonthView
import kotlin.math.min

/**
 * 高仿魅族日历布局
 * Created by huanghaibin on 2017/11/15.
 */
class SimpleMonthView(context: Context) : MonthView(context) {

    private var mRadius = 0

    override fun onPreviewHook() {
        mRadius = (min(mItemWidth.toDouble(), mItemHeight.toDouble()) / 5 * 2).toInt()
        mSchemePaint.style = Paint.Style.STROKE
    }

    override fun onLoopStart(x: Int, y: Int) {}

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
        return false
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSchemePaint)
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2
        when {
            isSelected -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, mSelectTextPaint)
            hasScheme -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint)
            else -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mCurMonthTextPaint else mOtherMonthTextPaint)
        }
    }
}
