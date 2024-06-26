package com.cygnet.caleviewdemo.view.mix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getMonthDaysCount
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getWeekCountBetweenBothCalendar
import com.cygnet.caleviewdemo.calendar.MonthView
import kotlin.math.min

/**
 * 演示一个变态需求的月视图
 * Created by huanghaibin on 2018/2/9.
 */
class MixMonthView(context: Context) : MonthView(context) {
    private var mRadius = 0

    /**
     * 自定义魅族标记的文本画笔
     */
    private val mTextPaint = Paint()

    /**
     * 24节气画笔
     */
    private val mSolarTermTextPaint = Paint()

    /**
     * 背景圆点
     */
    private val mPointPaint = Paint()

    /**
     * 今天的背景色
     */
    private val mCurrentDayPaint = Paint()

    /**
     * 圆点半径
     */
    private val mPointRadius: Float

    private val mPadding: Int

    private val mCircleRadius: Float

    /**
     * 自定义魅族标记的圆形背景
     */
    private val mSchemeBasicPaint = Paint()

    private val mSchemeBaseLine: Float

    init {
        mTextPaint.textSize = dipToPx(context, 8f).toFloat()
        mTextPaint.color = -0x1
        mTextPaint.isAntiAlias = true
        mTextPaint.isFakeBoldText = true

        mSolarTermTextPaint.color = -0xb76201
        mSolarTermTextPaint.isAntiAlias = true
        mSolarTermTextPaint.textAlign = Paint.Align.CENTER

        mSchemeBasicPaint.isAntiAlias = true
        mSchemeBasicPaint.style = Paint.Style.FILL
        mSchemeBasicPaint.textAlign = Paint.Align.CENTER
        mSchemeBasicPaint.isFakeBoldText = true
        mSchemeBasicPaint.color = Color.WHITE

        mCurrentDayPaint.isAntiAlias = true
        mCurrentDayPaint.style = Paint.Style.FILL
        mCurrentDayPaint.color = -0x151516

        mPointPaint.isAntiAlias = true
        mPointPaint.style = Paint.Style.FILL
        mPointPaint.textAlign = Paint.Align.CENTER
        mPointPaint.color = Color.RED

        mCircleRadius = dipToPx(getContext(), 7f).toFloat()

        mPadding = dipToPx(getContext(), 3f)

        mPointRadius = dipToPx(context, 2f).toFloat()

        val metrics = mSchemeBasicPaint.fontMetrics
        mSchemeBaseLine = mCircleRadius - metrics.descent + (metrics.bottom - metrics.top) / 2 + dipToPx(getContext(), 1f)
    }

    override fun onPreviewHook() {
        mSolarTermTextPaint.textSize = mCurMonthLunarTextPaint.textSize
        mRadius = (min(mItemWidth.toDouble(), mItemHeight.toDouble()) / 11 * 5).toInt()
    }

    override fun getClickCalendarPaddingObject(x: Float, y: Float, adjacentCalendar: Calendar): Any {
        return getWeekCountBetweenBothCalendar(
            mYear, 1, 1,
            mYear, adjacentCalendar.month, adjacentCalendar.day, weekStartWith
        )
    }

    override fun onDraw(canvas: Canvas) {
        val weekStart = getWeekCountBetweenBothCalendar(
            mYear, 1, 1,
            mYear, mMonth, 1, weekStartWith
        )
        val weekEnd = getWeekCountBetweenBothCalendar(
            mYear, 1, 1,
            mYear, mMonth, getMonthDaysCount(mYear, mMonth), weekStartWith
        )
        val width = dipToPx(context, 52f) //left-padding
        val cx = width / 2
        var cy = 0

        for (week in weekStart..weekEnd) {
            canvas.drawText(
                week.toString(), cx.toFloat(), mTextBaseLine + cy,
                mOtherMonthTextPaint
            )
            cy += mItemHeight
        }
        super.onDraw(canvas)
    }

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mSelectedPaint)
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        val isSelected = isSelected(calendar)
        if (isSelected) {
            mPointPaint.color = Color.WHITE
        }
        else {
            mPointPaint.color = Color.GRAY
        }

        canvas.drawCircle((x + mItemWidth / 2).toFloat(), (y + mItemHeight - 3 * mPadding).toFloat(), mPointRadius, mPointPaint)
    }

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        val top = y - mItemHeight / 6

        if (calendar.isCurrentDay && !isSelected) {
            canvas.drawCircle(cx.toFloat(), cy.toFloat(), mRadius.toFloat(), mCurrentDayPaint)
        }

        if (hasScheme) {
            canvas.drawCircle(x + mItemWidth - mPadding - mCircleRadius / 2, y + mPadding + mCircleRadius, mCircleRadius, mSchemeBasicPaint)
            mTextPaint.color = calendar.schemeColor
            canvas.drawText(calendar.scheme!!, x + mItemWidth - mPadding - mCircleRadius, y + mPadding + mSchemeBaseLine, mTextPaint)
        }

        //当然可以换成其它对应的画笔就不麻烦，
        if (calendar.isWeekend && calendar.isCurrentMonth) {
            mCurMonthTextPaint.color = -0xb76201
            mCurMonthLunarTextPaint.color = -0xb76201
            mSchemeTextPaint.color = -0xb76201
            mSchemeLunarTextPaint.color = -0xb76201
            mOtherMonthLunarTextPaint.color = -0xb76201
            mOtherMonthTextPaint.color = -0xb76201
        }
        else {
            mCurMonthTextPaint.color = -0xcccccd
            mCurMonthLunarTextPaint.color = -0x303031
            mSchemeTextPaint.color = -0xcccccd
            mSchemeLunarTextPaint.color = -0x303031

            mOtherMonthTextPaint.color = -0x1e1e1f
            mOtherMonthLunarTextPaint.color = -0x1e1e1f
        }

        if (isSelected) {
            canvas.drawText(
                calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                mSelectTextPaint
            )
            canvas.drawText(calendar.lunar!!, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10, mSelectedLunarTextPaint)
        }
        else if (hasScheme) {
            canvas.drawText(
                calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint
            )

            canvas.drawText(
                calendar.lunar!!, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10,
                if (!TextUtils.isEmpty(calendar.solarTerm)) mSolarTermTextPaint else mSchemeLunarTextPaint
            )
        }
        else {
            canvas.drawText(
                calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mCurMonthTextPaint else mOtherMonthTextPaint
            )

            canvas.drawText(
                calendar.lunar!!, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10,
                if (calendar.isCurrentDay) mCurDayLunarTextPaint else if (calendar.isCurrentMonth) if (!TextUtils.isEmpty(calendar.solarTerm)) mSolarTermTextPaint else mCurMonthLunarTextPaint else mOtherMonthLunarTextPaint
            )
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
