package com.cygnet.caleviewdemo.view.single

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.MonthView
import kotlin.math.min

/**
 * 高仿魅族日历布局
 * Created by huanghaibin on 2017/11/15.
 */
class SingleMonthView(context: Context) : MonthView(context) {

    private var mRadius = 0
    private val mRingPaint = Paint()
    private var mRingRadius = 0

    /**
     * 不可用画笔
     */
    private val mDisablePaint = Paint()

    private val mH: Int

    init {
        mRingPaint.isAntiAlias = true
        mRingPaint.color = mSchemePaint.color
        mRingPaint.style = Paint.Style.STROKE
        mRingPaint.strokeWidth = dipToPx(context, 1f).toFloat()
        setLayerType(LAYER_TYPE_SOFTWARE, mRingPaint)
        mRingPaint.setMaskFilter(BlurMaskFilter(30f, BlurMaskFilter.Blur.SOLID))

        mDisablePaint.color = -0x606061
        mDisablePaint.isAntiAlias = true
        mDisablePaint.strokeWidth = dipToPx(context, 2f).toFloat()
        mDisablePaint.isFakeBoldText = true

        mH = dipToPx(context, 18f)
    }

    override fun onPreviewHook() {
        mRadius = (min(mItemWidth.toDouble(), mItemHeight.toDouble()) / 6 * 2).toInt()
        mRingRadius = (min(mItemWidth.toDouble(), mItemHeight.toDouble()) / 5 * 2).toInt()
        mSelectTextPaint.textSize = dipToPx(context, 17f).toFloat()
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
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRingRadius.toFloat(), mRingPaint)

        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
//        int cx = x + mItemWidth / 2;
//        int cy = y + mItemHeight / 2;
//        canvas.drawCircle(cx, cy, mRadius, mSchemePaint);
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val baselineY = mTextBaseLine + y - dipToPx(context, 1f)
        val cx = x + mItemWidth / 2
        when {
            isSelected -> canvas.drawText(if (calendar.isCurrentDay) "今" else "选", cx.toFloat(), baselineY, mSelectTextPaint)
            hasScheme -> canvas.drawText(if (calendar.isCurrentDay) "今" else calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint)
            else -> canvas.drawText(if (calendar.isCurrentDay) "今" else calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mCurMonthTextPaint else mOtherMonthTextPaint)
        }

        //日期是否可用？拦截
        if (onCalendarIntercept(calendar)) {
            canvas.drawLine((x + mH).toFloat(), (y + mH).toFloat(), (x + mItemWidth - mH).toFloat(), (y + mItemHeight - mH).toFloat(), mDisablePaint)
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
