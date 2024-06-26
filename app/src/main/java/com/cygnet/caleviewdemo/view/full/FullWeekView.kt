package com.cygnet.caleviewdemo.view.full

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.WeekView

/**
 * 魅族周视图
 * Created by huanghaibin on 2017/11/29.
 */
class FullWeekView(context: Context) : WeekView(context) {

    private val mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSchemeBasicPaint = Paint()

    init {
        mRectPaint.style = Paint.Style.STROKE
        mRectPaint.strokeWidth = dipToPx(context, 0.5f).toFloat()
        mRectPaint.color = -0x77101011

        mSchemeBasicPaint.isAntiAlias = true
        mSchemeBasicPaint.style = Paint.Style.FILL
        mSchemeBasicPaint.textAlign = Paint.Align.CENTER
        mSchemeBasicPaint.color = -0x12acad
        mSchemeBasicPaint.isFakeBoldText = true

        //兼容硬件加速无效的代码
        setLayerType(LAYER_TYPE_SOFTWARE, mSchemeBasicPaint)
        //4.0以上硬件加速会导致无效
        mSelectedPaint.setMaskFilter(BlurMaskFilter(50f, BlurMaskFilter.Blur.SOLID))
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
        canvas.drawRect(x.toFloat(), 0f, (x + mItemWidth).toFloat(), mItemHeight.toFloat(), mSelectedPaint)
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int) {
        mSchemeBasicPaint.color = calendar.schemeColor
        val schemes: List<Calendar.Scheme>? = calendar.schemes
        if (schemes.isNullOrEmpty()) return
        val space = dipToPx(context, 2f)
        var indexY = mItemHeight - 2 * space
        val sw = dipToPx(context, (mItemWidth / 10).toFloat())
        val sh = dipToPx(context, 4f)
        for (scheme in schemes) {
            mSchemePaint.color = scheme.schemeColor
            canvas.drawRect((x + mItemWidth - sw - 2 * space).toFloat(), (indexY - sh).toFloat(), (x + mItemWidth - 2 * space).toFloat(), indexY.toFloat(), mSchemePaint)
            indexY = indexY - space - sh
        }
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, hasScheme: Boolean, isSelected: Boolean) {
        canvas.drawRect(x.toFloat(), 0f, (x + mItemWidth).toFloat(), mItemHeight.toFloat(), mRectPaint)
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
                canvas.drawText(calendar.day.toString(), cx.toFloat(), mTextBaseLine + top, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth && isInRange) mCurMonthTextPaint else mOtherMonthTextPaint)
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
