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

/**
 * 月视图基础控件,可自由继承实现
 * 可通过此扩展各种视图如：MonthView、RangeMonthView、MultiMonthView
 */
abstract class BaseMonthView(context: Context) : BaseView(context) {

    var mMonthViewPager: MonthViewPager? = null

    /**
     * 当前日历卡年份
     */
    protected var mYear: Int = 0

    /**
     * 当前日历卡月份
     */
    protected var mMonth: Int = 0

    /**
     * 日历的行数
     */
    protected var mLineCount: Int = 0

    /**
     * 日历高度
     */
    private var mHeight: Int = 0

    /**
     * 下个月偏移的数量
     */
    protected var mNextDiff: Int = 0

    /**
     * 初始化日期
     *
     * @param year  year
     * @param month month
     */
    fun initMonthWithDate(year: Int, month: Int) {
        mYear = year
        mMonth = month
        initCalendar()
        mHeight = CalendarUtil.getMonthViewHeight(
            year, month, mItemHeight, mDelegate.weekStart,
            mDelegate.monthViewShowMode
        )
    }

    /**
     * 初始化日历
     */
    private fun initCalendar() {
        val preDiff = CalendarUtil.getMonthViewStartDiff(mYear, mMonth, mDelegate.weekStart)
        val monthDayCount = CalendarUtil.getMonthDaysCount(mYear, mMonth)

        mNextDiff = CalendarUtil.getMonthEndDiff(mYear, mMonth, mDelegate.weekStart)
        mItems = CalendarUtil.initCalendarForMonthView(mYear, mMonth, mDelegate.currentDay, mDelegate.weekStart)

        mCurrentItem = if (mItems.contains(mDelegate.currentDay)) mItems.indexOf(mDelegate.currentDay)
        else mItems.indexOf(mDelegate.mSelectedCalendar)

        if (mCurrentItem > 0 && mDelegate.mCalendarInterceptListener != null &&
            mDelegate.mCalendarInterceptListener!!.onCalendarIntercept(mDelegate.mSelectedCalendar)
        ) mCurrentItem = -1

        mLineCount = if (mDelegate.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) 6
        else (preDiff + monthDayCount + mNextDiff) / 7

        addSchemesFromMap()
        invalidate()
    }

    protected val index: Calendar?
        /**
         * 获取点击选中的日期
         *
         * @return return
         */
        get() {
            if (mItemWidth == 0 || mItemHeight == 0) return null
            if (mX <= mDelegate.calendarPaddingLeft || mX >= width - mDelegate.calendarPaddingRight) {
                onClickCalendarPadding()
                return null
            }
            var indexX = (mX - mDelegate.calendarPaddingLeft).toInt() / mItemWidth
            if (indexX >= 7) indexX = 6
            val indexY = mY.toInt() / mItemHeight
            val position = indexY * 7 + indexX // 选择项
            if (position >= 0 && position < mItems.size) return mItems[position]
            return null
        }

    private fun onClickCalendarPadding() {
        if (mDelegate.mClickCalendarPaddingListener == null) return
        var calendar: Calendar? = null
        var indexX = (mX - mDelegate.calendarPaddingLeft).toInt() / mItemWidth
        if (indexX >= 7) indexX = 6
        val indexY = mY.toInt() / mItemHeight
        val position = indexY * 7 + indexX // 选择项
        if (position >= 0 && position < mItems.size) calendar = mItems[position]
        if (calendar == null) return
        mDelegate.mClickCalendarPaddingListener!!.onClickCalendarPadding(
            mX, mY, true, calendar,
            getClickCalendarPaddingObject(mX, mY, calendar)
        )
    }

    /**
     * 获取点击事件处的对象
     *
     * @param x                x
     * @param y                y
     * @param adjacentCalendar adjacent calendar
     * @return obj can as null
     */
    protected open fun getClickCalendarPaddingObject(x: Float, y: Float, adjacentCalendar: Calendar): Any? {
        return null
    }

    /**
     * 记录已经选择的日期
     *
     * @param calendar calendar
     */
    fun setSelectedCalendar(calendar: Calendar) {
        mCurrentItem = mItems.indexOf(calendar)
    }

    /**
     * 更新显示模式
     */
    fun updateShowMode() {
        mLineCount = CalendarUtil.getMonthViewLineCount(
            mYear, mMonth,
            mDelegate.weekStart, mDelegate.monthViewShowMode
        )
        mHeight = CalendarUtil.getMonthViewHeight(
            mYear, mMonth, mItemHeight, mDelegate.weekStart,
            mDelegate.monthViewShowMode
        )
        invalidate()
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        mHeight = CalendarUtil.getMonthViewHeight(
            mYear, mMonth, mItemHeight, mDelegate.weekStart,
            mDelegate.monthViewShowMode
        )
        initCalendar()
    }

    override fun updateItemHeight() {
        super.updateItemHeight()
        mHeight = CalendarUtil.getMonthViewHeight(
            mYear, mMonth, mItemHeight, mDelegate.weekStart,
            mDelegate.monthViewShowMode
        )
    }

    override fun updateCurrentDate() {
        if (mItems.contains(mDelegate.currentDay)) {
            for (item in mItems) { //添加操作
                item.isCurrentDay = false
            }
            val index = mItems.indexOf(mDelegate.currentDay)
            mItems[index].isCurrentDay = true
        }
        invalidate()
    }

    /**
     * 获取选中的下标
     *
     * @param calendar calendar
     * @return 获取选中的下标
     */
    fun getSelectedIndex(calendar: Calendar): Int {
        return mItems.indexOf(calendar)
    }

    override fun onMeasure(width: Int, height: Int) {
        var heightMeasureSpec = height
        if (mLineCount != 0) heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY)
        super.onMeasure(width, heightMeasureSpec)
    }

    /**
     * 开始绘制前的钩子，这里做一些初始化的操作，每次绘制只调用一次，性能高效
     * 没有需要可忽略不实现
     * 例如：
     * 1、需要绘制圆形标记事件背景，可以在这里计算半径
     * 2、绘制矩形选中效果，也可以在这里计算矩形宽和高
     */
    override fun onPreviewHook() {}

    /**
     * 循环绘制开始的回调，不需要可忽略
     * 绘制每个日历项的循环，用来计算baseLine、圆心坐标等都可以在这里实现
     *
     * @param x 日历Card x起点坐标
     * @param y 日历Card y起点坐标
     */
    protected open fun onLoopStart(x: Int, y: Int) {}

    override fun onDestroy() {}
}
