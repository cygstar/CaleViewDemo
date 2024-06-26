/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cygnet.caleviewdemo.calendar

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import kotlin.math.abs

/**
 * 基本的日历View，派生出MonthView 和 WeekView
 * Created by huanghaibin on 2018/1/23.
 */
abstract class BaseView(context: Context, attrs: AttributeSet? = null) : View(context, attrs), View.OnClickListener, OnLongClickListener {

    lateinit var mDelegate: CalendarViewDelegate

    /**
     * 当前月份日期的笔
     */
    protected var mCurMonthTextPaint: Paint = Paint()

    /**
     * 其它月份日期颜色
     */
    protected var mOtherMonthTextPaint: Paint = Paint()

    /**
     * 当前月份农历文本颜色
     */
    protected var mCurMonthLunarTextPaint: Paint = Paint()

    /**
     * 当前月份农历文本颜色
     */
    protected var mSelectedLunarTextPaint: Paint = Paint()

    /**
     * 其它月份农历文本颜色
     */
    protected var mOtherMonthLunarTextPaint: Paint = Paint()

    /**
     * 其它月份农历文本颜色
     */
    protected var mSchemeLunarTextPaint: Paint = Paint()

    /**
     * 标记的日期背景颜色画笔
     */
    protected var mSchemePaint: Paint = Paint()

    /**
     * 被选择的日期背景色
     */
    protected var mSelectedPaint: Paint = Paint()

    /**
     * 标记的文本画笔
     */
    protected var mSchemeTextPaint: Paint = Paint()

    /**
     * 选中的文本画笔
     */
    protected var mSelectTextPaint: Paint = Paint()

    /**
     * 当前日期文本颜色画笔
     */
    protected var mCurDayTextPaint: Paint = Paint()

    /**
     * 当前日期文本颜色画笔
     */
    protected var mCurDayLunarTextPaint: Paint = Paint()

    /**
     * 日历布局，需要在日历下方放自己的布局
     */
    var mParentLayout: CalendarLayout? = null

    /**
     * 日历项
     */
    lateinit var mItems: List<Calendar>

    /**
     * 每一项的高度
     */
    protected var mItemHeight: Int = 0

    /**
     * 每一项的宽度
     */
    protected var mItemWidth: Int = 0

    /**
     * Text的基线
     */
    protected var mTextBaseLine: Float = 0f

    /**
     * 点击的x、y坐标
     */
    protected var mX: Float = 0f
    protected var mY: Float = 0f

    /**
     * 是否点击
     */
    var isClick: Boolean = true

    /**
     * 当前点击项
     */
    var mCurrentItem: Int = -1

    /**
     * 周起始
     */
    private var mWeekStartWidth: Int = 0

    init {
        initPaint(context)
    }

    /**
     * 初始化配置
     *
     * @param context context
     */
    private fun initPaint(context: Context) {
        mCurMonthTextPaint.isAntiAlias = true
        mCurMonthTextPaint.textAlign = Paint.Align.CENTER
        mCurMonthTextPaint.color = -0xeeeeef
        mCurMonthTextPaint.isFakeBoldText = true
        mCurMonthTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mOtherMonthTextPaint.isAntiAlias = true
        mOtherMonthTextPaint.textAlign = Paint.Align.CENTER
        mOtherMonthTextPaint.color = -0x1e1e1f
        mOtherMonthTextPaint.isFakeBoldText = true
        mOtherMonthTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mCurMonthLunarTextPaint.isAntiAlias = true
        mCurMonthLunarTextPaint.textAlign = Paint.Align.CENTER

        mSelectedLunarTextPaint.isAntiAlias = true
        mSelectedLunarTextPaint.textAlign = Paint.Align.CENTER

        mOtherMonthLunarTextPaint.isAntiAlias = true
        mOtherMonthLunarTextPaint.textAlign = Paint.Align.CENTER

        mSchemeLunarTextPaint.isAntiAlias = true
        mSchemeLunarTextPaint.textAlign = Paint.Align.CENTER

        mSchemeTextPaint.isAntiAlias = true
        mSchemeTextPaint.style = Paint.Style.FILL
        mSchemeTextPaint.textAlign = Paint.Align.CENTER
        mSchemeTextPaint.color = -0x12acad
        mSchemeTextPaint.isFakeBoldText = true
        mSchemeTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mSelectTextPaint.isAntiAlias = true
        mSelectTextPaint.style = Paint.Style.FILL
        mSelectTextPaint.textAlign = Paint.Align.CENTER
        mSelectTextPaint.color = -0x12acad
        mSelectTextPaint.isFakeBoldText = true
        mSelectTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mSchemePaint.isAntiAlias = true
        mSchemePaint.style = Paint.Style.FILL
        mSchemePaint.strokeWidth = 2f
        mSchemePaint.color = -0x101011

        mCurDayTextPaint.isAntiAlias = true
        mCurDayTextPaint.textAlign = Paint.Align.CENTER
        mCurDayTextPaint.color = Color.RED
        mCurDayTextPaint.isFakeBoldText = true
        mCurDayTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mCurDayLunarTextPaint.isAntiAlias = true
        mCurDayLunarTextPaint.textAlign = Paint.Align.CENTER
        mCurDayLunarTextPaint.color = Color.RED
        mCurDayLunarTextPaint.isFakeBoldText = true
        mCurDayLunarTextPaint.textSize = CalendarUtil.dipToPx(context, TEXT_SIZE.toFloat()).toFloat()

        mSelectedPaint.isAntiAlias = true
        mSelectedPaint.style = Paint.Style.FILL
        mSelectedPaint.strokeWidth = 2f

        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    /**
     * 初始化所有UI配置
     *
     * @param delegate delegate
     */
    fun setup(delegate: CalendarViewDelegate) {
        this.mDelegate = delegate
        mWeekStartWidth = mDelegate.weekStart
        updateStyle()
        updateItemHeight()

        initPaint()
    }

    fun updateStyle() {
        mCurDayTextPaint.color = mDelegate.curDayTextColor
        mCurDayLunarTextPaint.color = mDelegate.curDayLunarTextColor
        mCurMonthTextPaint.color = mDelegate.currentMonthTextColor
        mOtherMonthTextPaint.color = mDelegate.otherMonthTextColor
        mCurMonthLunarTextPaint.color = mDelegate.currentMonthLunarTextColor
        mSelectedLunarTextPaint.color = mDelegate.selectedLunarTextColor
        mSelectTextPaint.color = mDelegate.selectedTextColor
        mOtherMonthLunarTextPaint.color = mDelegate.otherMonthLunarTextColor
        mSchemeLunarTextPaint.color = mDelegate.schemeLunarTextColor
        mSchemePaint.color = mDelegate.schemeThemeColor
        mSchemeTextPaint.color = mDelegate.schemeTextColor
        mCurMonthTextPaint.textSize = mDelegate.dayTextSize.toFloat()
        mOtherMonthTextPaint.textSize = mDelegate.dayTextSize.toFloat()
        mCurDayTextPaint.textSize = mDelegate.dayTextSize.toFloat()
        mSchemeTextPaint.textSize = mDelegate.dayTextSize.toFloat()
        mSelectTextPaint.textSize = mDelegate.dayTextSize.toFloat()

        mCurMonthLunarTextPaint.textSize = mDelegate.lunarTextSize.toFloat()
        mSelectedLunarTextPaint.textSize = mDelegate.lunarTextSize.toFloat()
        mCurDayLunarTextPaint.textSize = mDelegate.lunarTextSize.toFloat()
        mOtherMonthLunarTextPaint.textSize = mDelegate.lunarTextSize.toFloat()
        mSchemeLunarTextPaint.textSize = mDelegate.lunarTextSize.toFloat()

        mSelectedPaint.style = Paint.Style.FILL
        mSelectedPaint.color = mDelegate.selectedThemeColor
    }

    open fun updateItemHeight() {
        this.mItemHeight = mDelegate.calendarItemHeight
        val metrics = mCurMonthTextPaint.fontMetrics
        mTextBaseLine = mItemHeight / 2 - metrics.descent + (metrics.bottom - metrics.top) / 2
    }

    /**
     * 移除事件
     */
    private fun removeSchemes() {
        for (item in mItems) {
            item.scheme = ""
            item.schemeColor = 0
            item.schemes = null
        }
    }

    /**
     * 添加事件标记，来自Map
     */
    fun addSchemesFromMap() {
        if (mDelegate.mSchemeDatesMap == null || mDelegate.mSchemeDatesMap!!.isEmpty()) return
        for (item in mItems) {
            if (mDelegate.mSchemeDatesMap!!.containsKey(item.toString())) {
                val dlg = mDelegate.mSchemeDatesMap!![item.toString()] ?: continue
                item.scheme = if (TextUtils.isEmpty(dlg.scheme)) mDelegate.schemeText else dlg.scheme
                item.schemeColor = dlg.schemeColor
                item.schemes = dlg.schemes
            }
            else {
                item.scheme = ""
                item.schemeColor = 0
                item.schemes = null
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount > 1) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mX = event.x
                mY = event.y
                isClick = true
            }

            MotionEvent.ACTION_MOVE -> {
                val mDY: Float
                if (isClick) {
                    mDY = event.y - mY
                    isClick = abs(mDY.toDouble()) <= 50
                }
            }

            MotionEvent.ACTION_UP -> {
                mX = event.x
                mY = event.y
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 开始绘制前的钩子，这里做一些初始化的操作，每次绘制只调用一次，性能高效
     * 没有需要可忽略不实现
     * 例如：
     * 1、需要绘制圆形标记事件背景，可以在这里计算半径
     * 2、绘制矩形选中效果，也可以在这里计算矩形宽和高
     */
    protected open fun onPreviewHook() {}

    /**
     * 是否是选中的
     *
     * @param calendar calendar
     * @return true or false
     */
    protected fun isSelected(calendar: Calendar): Boolean {
        return mItems.indexOf(calendar) == mCurrentItem
    }

    /**
     * 更新事件
     */
    fun update() {
        if (mDelegate.mSchemeDatesMap == null || mDelegate.mSchemeDatesMap!!.isEmpty()) { //清空操作
            removeSchemes()
            invalidate()
            return
        }
        addSchemesFromMap()
        invalidate()
    }

    /**
     * 是否拦截日期，此设置续设置mCalendarInterceptListener
     *
     * @param calendar calendar
     * @return 是否拦截日期
     */
    protected fun onCalendarIntercept(calendar: Calendar): Boolean {
        return mDelegate.mCalendarInterceptListener != null && mDelegate.mCalendarInterceptListener!!.onCalendarIntercept(calendar)
    }

    /**
     * 是否在日期范围内
     *
     * @param calendar calendar
     * @return 是否在日期范围内
     */
    protected fun isInRange(calendar: Calendar): Boolean {
        return CalendarUtil.isCalendarInRange(calendar, mDelegate)
    }

    /**
     * 跟新当前日期
     */
    abstract fun updateCurrentDate()

    /**
     * 销毁
     */
    abstract fun onDestroy()

    protected val weekStartWith: Int
        get() = mDelegate.weekStart

    protected val calendarPaddingLeft: Int
        get() = mDelegate.calendarPaddingLeft

    protected val calendarPaddingRight: Int
        get() = mDelegate.calendarPaddingRight

    /**
     * 初始化画笔相关
     */
    private fun initPaint() {}

    companion object {
        /**
         * 字体大小
         */
        const val TEXT_SIZE: Int = 14
    }
}
