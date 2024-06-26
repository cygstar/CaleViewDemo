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
 * 最基础周视图，因为日历UI采用热插拔实现，所以这里必须继承实现，达到UI一致即可
 * 可通过此扩展各种视图如：WeekView、RangeWeekView
 */
abstract class BaseWeekView(context: Context) : BaseView(context) {
    /**
     * 初始化周视图控件
     *
     * @param calendar calendar
     */
    fun setup(calendar: Calendar) {
        mItems = CalendarUtil.initCalendarForWeekView(calendar, mDelegate, mDelegate.weekStart)
        addSchemesFromMap()
        invalidate()
    }

    /**
     * 记录已经选择的日期
     *
     * @param calendar calendar
     */
    fun setSelectedCalendar(calendar: Calendar) {
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_SINGLE && calendar != mDelegate.mSelectedCalendar) {
            return
        }
        mCurrentItem = mItems.indexOf(calendar)
    }

    /**
     * 周视图切换点击默认位置
     *
     * @param calendar calendar
     * @param isNotice isNotice
     */
    fun performClickCalendar(calendar: Calendar, isNotice: Boolean) {
        if (mParentLayout == null || mDelegate.mInnerListener == null || mItems.isEmpty()) return

        var week = CalendarUtil.getWeekViewIndexFromCalendar(calendar, mDelegate.weekStart)
        if (mItems.contains(mDelegate.currentDay)) week = CalendarUtil.getWeekViewIndexFromCalendar(mDelegate.currentDay, mDelegate.weekStart)

        var curIndex = week
        var currentCalendar = mItems[week]
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            if (mItems.contains(mDelegate.mSelectedCalendar)) currentCalendar = mDelegate.mSelectedCalendar else mCurrentItem = -1
        }

        if (!isInRange(currentCalendar)) {
            curIndex = getEdgeIndex(isMinRangeEdge(currentCalendar))
            currentCalendar = mItems[curIndex]
        }

        currentCalendar.isCurrentDay = currentCalendar == mDelegate.currentDay
        mDelegate.mInnerListener!!.onWeekDateSelected(currentCalendar, false)
        mParentLayout!!.updateSelectWeek(CalendarUtil.getWeekFromDayInMonth(currentCalendar, mDelegate.weekStart))

        if (mDelegate.mCalendarSelectListener != null && isNotice && mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            mDelegate.mCalendarSelectListener!!.onCalendarSelect(currentCalendar, false)
        }

        mParentLayout!!.updateContentViewTranslateY()
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) mCurrentItem = curIndex

        if (!mDelegate.isShowYearSelectedLayout && calendar.year != mDelegate.mIndexCalendar.year && mDelegate.mYearChangeListener != null) {
            mDelegate.mYearChangeListener!!.onYearChange(mDelegate.mIndexCalendar.year)
        }

        mDelegate.mIndexCalendar = currentCalendar
        invalidate()
    }

    /**
     * 是否是最小访问边界了
     *
     * @param calendar calendar
     * @return 是否是最小访问边界了
     */
    private fun isMinRangeEdge(calendar: Calendar): Boolean {
        val cale = java.util.Calendar.getInstance()
        cale[mDelegate.minYear, mDelegate.minYearMonth - 1] = mDelegate.minYearDay
        val minTime = cale.timeInMillis
        cale[calendar.year, calendar.month - 1] = calendar.day
        val curTime = cale.timeInMillis
        return curTime < minTime
    }

    /**
     * 获得边界范围内下标
     *
     * @param isMinEdge isMinEdge
     * @return 获得边界范围内下标
     */
    private fun getEdgeIndex(isMinEdge: Boolean): Int {
        for (i in mItems.indices) {
            val item = mItems[i]
            val isInRange = isInRange(item)
            if (isMinEdge && isInRange) return i else if (!isMinEdge && !isInRange) return i - 1
        }
        return if (isMinEdge) 6 else 0
    }

    protected val index: Calendar?
        /**
         * 获取点击的日历
         *
         * @return 获取点击的日历
         */
        get() {
            if (mX <= mDelegate.calendarPaddingLeft || mX >= width - mDelegate.calendarPaddingRight) {
                onClickCalendarPadding()
                return null
            }

            var indexX = (mX - mDelegate.calendarPaddingLeft).toInt() / mItemWidth
            if (indexX >= 7) {
                indexX = 6
            }
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
            mX, mY, false, calendar, getClickCalendarPaddingObject(mX, mY, calendar)
        )
    }

    /**
     * / **
     * 获取点击事件处的对象
     *
     * @param x x
     * @param y y
     * @param adjacentCalendar adjacent calendar
     * @return obj can as null
     */
    protected open fun getClickCalendarPaddingObject(x: Float, y: Float, adjacentCalendar: Calendar): Any? {
        return null
    }

    /**
     * 更新显示模式
     */
    fun updateShowMode() {
        invalidate()
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        val position = tag as Int
        val calendar = CalendarUtil.getFirstCalendarStartWithMinCalendar(
            mDelegate.minYear, mDelegate.minYearMonth, mDelegate.minYearDay, position + 1, mDelegate.weekStart
        )
        setSelectedCalendar(mDelegate.mSelectedCalendar)
        setup(calendar)
    }

    /**
     * 更新当选模式
     */
    fun updateSingleSelect() {
        if (!mItems.contains(mDelegate.mSelectedCalendar)) {
            mCurrentItem = -1
            invalidate()
        }
    }

    override fun updateCurrentDate() {
        if (mItems.contains(mDelegate.currentDay)) {
            for (item in mItems) {  //添加操作
                item.isCurrentDay = false
            }
            val index = mItems.indexOf(mDelegate.currentDay)
            mItems[index].isCurrentDay = true
        }
        invalidate()
    }

    override fun onMeasure(width: Int, height: Int) {
        var heightMeasureSpec = height
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemHeight, MeasureSpec.EXACTLY)
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
     */
    protected fun onLoopStart(x: Int) {}

    override fun onDestroy() {}
}
