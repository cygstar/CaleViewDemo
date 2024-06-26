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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.cygnet.caleviewdemo.R

/**
 * 日历布局
 * 各个类使用包权限，避免不必要的public
 */
class CalendarView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    /**
     * 抽取自定义属性
     */
    private val mDelegate: CalendarViewDelegate = CalendarViewDelegate(context, attrs)

    /**
     * MonthViewPager
     *
     * @return 获得月视图
     */
    /**
     * 自定义自适应高度的ViewPager
     */
    var monthViewPager: MonthViewPager? = null

    /**
     * 获得周视图
     *
     * @return 获得周视图
     */
    /**
     * 日历周视图
     */
    var weekViewPager: WeekViewPager? = null

    /**
     * 星期栏的线
     */
    private var mWeekLine: View? = null

    /**
     * 月份快速选取
     */
    private var mYearViewPager: YearViewPager? = null

    /**
     * 星期栏
     */
    private var mWeekBar: WeekBar? = null

    /**
     * 日历外部收缩布局
     */
    var mParentLayout: CalendarLayout? = null

    init {
        init(context)
    }

    /**
     * 初始化
     *
     * @param context context
     */
    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.cv_layout_calendar_view, this, true)
        val frameContent = findViewById<FrameLayout>(R.id.frameContent)
        this.weekViewPager = findViewById(R.id.vp_week)
        weekViewPager?.setup(mDelegate)

        try {
            val constructor = mDelegate.weekBarClass?.getConstructor(Context::class.java)
            mWeekBar = constructor?.newInstance(getContext()) as WeekBar
        }
        catch (_: Exception) {
        }

        frameContent.addView(mWeekBar, 2)
        mWeekBar?.setup(mDelegate)
        mWeekBar?.onWeekStartChange(mDelegate.weekStart)

        this.mWeekLine = findViewById(R.id.line)
        mWeekLine?.setBackgroundColor(mDelegate.weekLineBackground)
        val lineParams = mWeekLine?.layoutParams as LayoutParams
        lineParams.setMargins(
            mDelegate.weekLineMargin,
            mDelegate.weekBarHeight,
            mDelegate.weekLineMargin,
            0
        )
        mWeekLine?.setLayoutParams(lineParams)

        this.monthViewPager = findViewById(R.id.vp_month)
        monthViewPager?.mWeekPager = this.weekViewPager
        monthViewPager?.mWeekBar = mWeekBar
        val params = monthViewPager?.layoutParams as LayoutParams
        params.setMargins(0, mDelegate.weekBarHeight + CalendarUtil.dipToPx(context, 1f), 0, 0)
        weekViewPager?.setLayoutParams(params)

        mYearViewPager = findViewById(R.id.selectLayout)
        mYearViewPager?.setPadding(mDelegate.yearViewPaddingLeft, 0, mDelegate.yearViewPaddingRight, 0)
        mYearViewPager?.setBackgroundColor(mDelegate.yearViewBackground)
        mYearViewPager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (weekViewPager?.visibility == VISIBLE) return
                if (mDelegate.mYearChangeListener != null) mDelegate.mYearChangeListener?.onYearChange(position + mDelegate.minYear)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        mDelegate.mInnerListener = object : OnInnerDateSelectedListener {
            /**
             * 月视图选择事件
             * @param calendar calendar
             * @param isClick  是否是点击
             */
            override fun onMonthDateSelected(calendar: Calendar, isClick: Boolean) {
                if (calendar.year == mDelegate.currentDay.year && calendar.month == mDelegate.currentDay.month && monthViewPager?.currentItem != mDelegate.mCurrentMonthViewItem) {
                    return
                }
                mDelegate.mIndexCalendar = calendar
                if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick) mDelegate.mSelectedCalendar = calendar
                weekViewPager?.updateSelected(mDelegate.mIndexCalendar, false)
                monthViewPager?.updateSelected()
                if (mWeekBar != null && (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick)) {
                    mWeekBar?.onDateSelected(calendar, mDelegate.weekStart, isClick)
                }
            }

            /**
             * 周视图选择事件
             * @param calendar calendar
             * @param isClick 是否是点击
             */
            override fun onWeekDateSelected(calendar: Calendar, isClick: Boolean) {
                mDelegate.mIndexCalendar = calendar
                if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick || mDelegate.mIndexCalendar == mDelegate.mSelectedCalendar) {
                    mDelegate.mSelectedCalendar = calendar
                }
                val y = calendar.year - mDelegate.minYear
                val position = 12 * y + mDelegate.mIndexCalendar.month - mDelegate.minYearMonth
                weekViewPager?.updateSingleSelect()
                monthViewPager?.setCurrentItem(position, false)
                monthViewPager?.updateSelected()
                if (mWeekBar != null && (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick || mDelegate.mIndexCalendar == mDelegate.mSelectedCalendar)) {
                    mWeekBar?.onDateSelected(calendar, mDelegate.weekStart, isClick)
                }
            }
        }

        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            if (isInRange(mDelegate.currentDay)) mDelegate.mSelectedCalendar = mDelegate.createCurrentDate() else mDelegate.mSelectedCalendar = mDelegate.minRangeCalendar
        }
        else mDelegate.mSelectedCalendar = Calendar()

        mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar
        mWeekBar?.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.weekStart, false)

        monthViewPager?.setup(mDelegate)
        monthViewPager?.currentItem = mDelegate.mCurrentMonthViewItem
        mYearViewPager?.setOnMonthSelectedListener { year, month ->
            val position = 12 * (year - mDelegate.minYear) + month - mDelegate.minYearMonth
            closeSelectLayout(position)
            mDelegate.isShowYearSelectedLayout = false
        }
        mYearViewPager?.setup(mDelegate)
        weekViewPager?.updateSelected(mDelegate.createCurrentDate(), false)
    }

    /**
     * 设置日期范围
     *
     * @param minYear      最小年份
     * @param minYearMonth 最小年份对应月份
     * @param minYearDay   最小年份对应天
     * @param maxYear      最大月份
     * @param maxYearMonth 最大月份对应月份
     * @param maxYearDay   最大月份对应天
     */
    fun setRange(
        minYear: Int, minYearMonth: Int, minYearDay: Int,
        maxYear: Int, maxYearMonth: Int, maxYearDay: Int,
    ) {
        if (CalendarUtil.compareTo(minYear, minYearMonth, minYearDay, maxYear, maxYearMonth, maxYearDay) > 0) return
        mDelegate.setRange(
            minYear, minYearMonth, minYearDay,
            maxYear, maxYearMonth, maxYearDay
        )
        weekViewPager?.notifyDataSetChanged()
        mYearViewPager?.notifyDataSetChanged()
        monthViewPager?.notifyDataSetChanged()
        if (!isInRange(mDelegate.mSelectedCalendar)) {
            mDelegate.mSelectedCalendar = mDelegate.minRangeCalendar
            mDelegate.updateSelectCalendarScheme()
            mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar
        }
        weekViewPager?.updateRange()
        monthViewPager?.updateRange()
        mYearViewPager?.updateRange()
    }

    val curDay: Int
        /**
         * 获取当天
         *
         * @return 返回今天
         */
        get() = mDelegate.currentDay.day

    val curMonth: Int
        /**
         * 获取本月
         *
         * @return 返回本月
         */
        get() = mDelegate.currentDay.month

    val curYear: Int
        /**
         * 获取本年
         *
         * @return 返回本年
         */
        get() = mDelegate.currentDay.year

    /**
     * 打开日历年月份快速选择
     *
     * @param year 年
     */
    fun showYearSelectLayout(year: Int) {
        showSelectLayout(year)
    }

    /**
     * 打开日历年月份快速选择
     * 请使用 showYearSelectLayout(final int year) 代替，这个没什么，越来越规范
     *
     * @param year 年
     */
    private fun showSelectLayout(year: Int) {
        if (mParentLayout != null && mParentLayout?.mContentView != null) {
            if (!mParentLayout!!.isExpand) {
                mParentLayout?.expand()
                //return;
            }
        }
        weekViewPager?.visibility = GONE
        mDelegate.isShowYearSelectedLayout = true
        if (mParentLayout != null) mParentLayout?.hideContentView()
        mWeekBar!!.animate()
                .translationY(-mWeekBar!!.height.toFloat())
                .setInterpolator(LinearInterpolator())
                .setDuration(260)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mWeekBar?.visibility = GONE
                        mYearViewPager?.visibility = VISIBLE
                        mYearViewPager?.scrollToYear(year, false)
                        if (mParentLayout != null && mParentLayout?.mContentView != null) {
                            mParentLayout?.expand()
                        }
                    }
                })

        monthViewPager!!.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(260)
                .setInterpolator(LinearInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        if (mDelegate.mYearViewChangeListener != null) {
                            mDelegate.mYearViewChangeListener?.onYearViewChange(false)
                        }
                    }
                })
    }

    private val isYearSelectLayoutVisible: Boolean
        /**
         * 年月份选择视图是否打开
         *
         * @return true or false
         */
        get() = mYearViewPager?.visibility == VISIBLE

    /**
     * 关闭年月视图选择布局
     */
    fun closeYearSelectLayout() {
        if (mYearViewPager?.visibility == GONE) return
        val position = 12 * (mDelegate.mSelectedCalendar.year - mDelegate.minYear) + mDelegate.mSelectedCalendar.month - mDelegate.minYearMonth
        closeSelectLayout(position)
        mDelegate.isShowYearSelectedLayout = false
    }

    /**
     * 关闭日历布局，同时会滚动到指定的位置
     *
     * @param position 某一年
     */
    private fun closeSelectLayout(position: Int) {
        mYearViewPager?.visibility = GONE
        mWeekBar?.visibility = VISIBLE
        if (position == monthViewPager?.currentItem) {
            if (mDelegate.mCalendarSelectListener != null && mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_SINGLE) {
                mDelegate.mCalendarSelectListener?.onCalendarSelect(mDelegate.mSelectedCalendar, false)
            }
        }
        else monthViewPager?.setCurrentItem(position, false)
        mWeekBar!!.animate()
                .translationY(0f)
                .setInterpolator(LinearInterpolator())
                .setDuration(280)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mWeekBar?.visibility = VISIBLE
                    }
                })
        monthViewPager!!.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(180)
                .setInterpolator(LinearInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        if (mDelegate.mYearViewChangeListener != null) mDelegate.mYearViewChangeListener?.onYearViewChange(true)
                        if (mParentLayout != null) {
                            mParentLayout?.showContentView()
                            if (mParentLayout!!.isExpand) monthViewPager?.visibility = VISIBLE
                            else {
                                weekViewPager?.visibility = VISIBLE
                                mParentLayout?.shrink()
                            }
                        }
                        else monthViewPager?.visibility = VISIBLE
                        monthViewPager!!.clearAnimation()
                    }
                })
    }

    /**
     * 滚动到当前
     *
     * @param smoothScroll smoothScroll
     */
    /**
     * 滚动到当前
     */
    fun scrollToCurrent(smoothScroll: Boolean = false) {
        if (!isInRange(mDelegate.currentDay)) return
        val calendar = mDelegate.createCurrentDate()
        if (mDelegate.mCalendarInterceptListener != null && mDelegate.mCalendarInterceptListener!!.onCalendarIntercept(calendar)) {
            mDelegate.mCalendarInterceptListener?.onCalendarInterceptClick(calendar, false)
            return
        }
        mDelegate.mSelectedCalendar = mDelegate.createCurrentDate()
        mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar
        mDelegate.updateSelectCalendarScheme()
        mWeekBar?.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.weekStart, false)
        if (monthViewPager?.visibility == VISIBLE) {
            monthViewPager?.scrollToCurrent(smoothScroll)
            weekViewPager?.updateSelected(mDelegate.mIndexCalendar, false)
        }
        else weekViewPager?.scrollToCurrent(smoothScroll)
        mYearViewPager?.scrollToYear(mDelegate.currentDay.year, smoothScroll)
    }

    /**
     * 滚动到下一个月
     *
     * @param smoothScroll smoothScroll
     */
    /**
     * 滚动到下一个月
     */
    fun scrollToNext(smoothScroll: Boolean = false) {
        if (isYearSelectLayoutVisible) mYearViewPager?.setCurrentItem(mYearViewPager!!.currentItem + 1, smoothScroll)
        else if (weekViewPager?.visibility == VISIBLE) weekViewPager?.setCurrentItem(weekViewPager!!.currentItem + 1, smoothScroll)
        else monthViewPager?.setCurrentItem(monthViewPager!!.currentItem + 1, smoothScroll)
    }

    /**
     * 滚动到上一个月
     *
     * @param smoothScroll smoothScroll
     */
    /**
     * 滚动到上一个月
     */
    fun scrollToPre(smoothScroll: Boolean = false) {
        if (isYearSelectLayoutVisible) mYearViewPager?.setCurrentItem(mYearViewPager!!.currentItem - 1, smoothScroll)
        else if (weekViewPager?.visibility == VISIBLE) weekViewPager?.setCurrentItem(weekViewPager!!.currentItem - 1, smoothScroll)
        else monthViewPager?.setCurrentItem(monthViewPager!!.currentItem - 1, smoothScroll)
    }

    /**
     * 滚动到选择的日历
     */
    fun scrollToSelectCalendar() {
        if (!mDelegate.mSelectedCalendar.isAvailable) return
        scrollToCalendar(
            mDelegate.mSelectedCalendar.year,
            mDelegate.mSelectedCalendar.month,
            mDelegate.mSelectedCalendar.day,
            smoothScroll = false,
            invokeListener = true
        )
    }

    /**
     * 滚动到指定日期
     *
     * @param year           year
     * @param month          month
     * @param day            day
     * @param smoothScroll   smoothScroll
     * @param invokeListener 调用日期事件
     */
    /**
     * 滚动到指定日期
     *
     * @param year  year
     * @param month month
     * @param day   day
     */
    /**
     * 滚动到指定日期
     *
     * @param year         year
     * @param month        month
     * @param day          day
     * @param smoothScroll smoothScroll
     */
    private fun scrollToCalendar(year: Int, month: Int, day: Int, smoothScroll: Boolean = false, invokeListener: Boolean = true) {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        if (!calendar.isAvailable) return
        if (!isInRange(calendar)) return
        if (mDelegate.mCalendarInterceptListener != null && mDelegate.mCalendarInterceptListener!!.onCalendarIntercept(calendar)) {
            mDelegate.mCalendarInterceptListener?.onCalendarInterceptClick(calendar, false)
            return
        }

        if (weekViewPager?.visibility == VISIBLE) weekViewPager?.scrollToCalendar(year, month, day, smoothScroll, invokeListener)
        else monthViewPager?.scrollToCalendar(year, month, day, smoothScroll, invokeListener)
    }

    /**
     * 滚动到某一年
     *
     * @param year         快速滚动的年份
     * @param smoothScroll smoothScroll
     */
    /**
     * 滚动到某一年
     *
     * @param year 快速滚动的年份
     */
    fun scrollToYear(year: Int, smoothScroll: Boolean = false) {
        if (mYearViewPager?.visibility != VISIBLE) return
        mYearViewPager?.scrollToYear(year, smoothScroll)
    }

    /**
     * 设置月视图是否可滚动
     *
     * @param monthViewScrollable 设置月视图是否可滚动
     */
    fun setMonthViewScrollable(monthViewScrollable: Boolean) {
        mDelegate.isMonthViewScrollable = monthViewScrollable
    }

    /**
     * 设置周视图是否可滚动
     *
     * @param weekViewScrollable 设置周视图是否可滚动
     */
    fun setWeekViewScrollable(weekViewScrollable: Boolean) {
        mDelegate.isWeekViewScrollable = weekViewScrollable
    }

    /**
     * 设置年视图是否可滚动
     *
     * @param yearViewScrollable 设置年视图是否可滚动
     */
    fun setYearViewScrollable(yearViewScrollable: Boolean) {
        mDelegate.isYearViewScrollable = yearViewScrollable
    }

    fun setDefaultMonthViewSelectDay() {
        mDelegate.defaultCalendarSelectDay = CalendarViewDelegate.FIRST_DAY_OF_MONTH
    }

    fun setLastMonthViewSelectDay() {
        mDelegate.defaultCalendarSelectDay = CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY
    }

    fun setLastMonthViewSelectDayIgnoreCurrent() {
        mDelegate.defaultCalendarSelectDay = CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY_IGNORE_CURRENT
    }

    /**
     * 清除选择范围
     */
    fun clearSelectRange() {
        mDelegate.clearSelectRange()
        monthViewPager?.clearSelectRange()
        weekViewPager?.clearSelectRange()
    }

    /**
     * 清除单选
     */
    fun clearSingleSelect() {
        mDelegate.mSelectedCalendar = Calendar()
        monthViewPager?.clearSingleSelect()
        weekViewPager?.clearSingleSelect()
    }

    /**
     * 清除多选
     */
    fun clearMultiSelect() {
        mDelegate.mSelectedCalendars.clear()
        monthViewPager?.clearMultiSelect()
        weekViewPager?.clearMultiSelect()
    }

    /**
     * 添加选择
     *
     * @param calendars calendars
     */
    fun putMultiSelect(vararg calendars: Calendar?) {
        if (calendars.isEmpty()) return
        for (cale in calendars) {
            if (cale == null || mDelegate.mSelectedCalendars.containsKey(cale.toString())) continue
            mDelegate.mSelectedCalendars[cale.toString()] = cale
        }
        update()
    }

    /**
     * 清楚一些多选日期
     *
     * @param calendars calendars
     */
    fun removeMultiSelect(vararg calendars: Calendar?) {
        if (calendars.isEmpty()) return
        for (cale in calendars) {
            if (cale == null) continue
            if (mDelegate.mSelectedCalendars.containsKey(cale.toString())) {
                mDelegate.mSelectedCalendars.remove(cale.toString())
            }
        }
        update()
    }

    val multiSelectCalendars: List<Calendar>
        get() {
            val calendars: MutableList<Calendar> = ArrayList()
            if (mDelegate.mSelectedCalendars.isEmpty()) return calendars
            calendars.addAll(mDelegate.mSelectedCalendars.values)
            calendars.sort()
            return calendars
        }

    val selectCalendarRange: List<Calendar>?
        /**
         * 获取选中范围
         *
         * @return return
         */
        get() = mDelegate.selectCalendarRange

    /**
     * 设置月视图项高度
     *
     * @param calendarItemHeight MonthView item height
     */
    fun setCalendarItemHeight(calendarItemHeight: Int) {
        if (mDelegate.calendarItemHeight == calendarItemHeight) return
        mDelegate.calendarItemHeight = calendarItemHeight
        monthViewPager?.updateItemHeight()
        weekViewPager?.updateItemHeight()
        if (mParentLayout == null) return
        mParentLayout?.updateCalendarItemHeight()
    }

    /**
     * 设置月视图
     *
     * @param cls MonthView.class
     */
    fun setMonthView(cls: Class<*>?) {
        if (cls == null) return
        if (mDelegate.monthViewClass == cls) return
        mDelegate.monthViewClass = cls
        monthViewPager?.updateMonthViewClass()
    }

    /**
     * 设置周视图
     *
     * @param cls WeekView.class
     */
    fun setWeekView(cls: Class<*>?) {
        if (cls == null) return
        if (mDelegate.weekBarClass == cls) return
        mDelegate.weekViewClass = cls
        weekViewPager?.updateWeekViewClass()
    }

    /**
     * 设置周栏视图
     *
     * @param cls WeekBar.class
     */
    fun setWeekBar(cls: Class<*>?) {
        if (cls == null) return
        if (mDelegate.weekBarClass == cls) return
        mDelegate.weekBarClass = cls
        val frameContent = findViewById<FrameLayout>(R.id.frameContent)
        frameContent.removeView(mWeekBar)

        try {
            val constructor = cls.getConstructor(Context::class.java)
            mWeekBar = constructor.newInstance(context) as WeekBar
        }
        catch (_: Exception) {
        }
        frameContent.addView(mWeekBar, 2)
        mWeekBar?.setup(mDelegate)
        mWeekBar?.onWeekStartChange(mDelegate.weekStart)
        monthViewPager?.mWeekBar = mWeekBar
        mWeekBar?.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.weekStart, false)
    }

    /**
     * 添加日期拦截事件
     * 使用此方法，只能基于select_mode = single_mode
     * 否则的话，如果标记全部日期为不可点击，那是没有意义的，
     * 框架本身也不可能在滑动的过程中全部去判断每个日期的可点击性
     *
     * @param listener listener
     */
    fun setOnCalendarInterceptListener(listener: OnCalendarInterceptListener?) {
        if (listener == null) mDelegate.mCalendarInterceptListener = null
        if (listener == null || mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) return
        mDelegate.mCalendarInterceptListener = listener
        if (!listener.onCalendarIntercept(mDelegate.mSelectedCalendar)) return
        mDelegate.mSelectedCalendar = Calendar()
    }

    /**
     * 点击视图Padding位置的事件
     *
     * @param listener listener
     */
    fun setOnClickCalendarPaddingListener(listener: OnClickCalendarPaddingListener?) {
        if (listener == null) mDelegate.mClickCalendarPaddingListener = null
        if (listener == null) return
        mDelegate.mClickCalendarPaddingListener = listener
    }

    /**
     * 年份改变事件
     *
     * @param listener listener
     */
    fun setOnYearChangeListener(listener: OnYearChangeListener) {
        mDelegate.mYearChangeListener = listener
    }

    /**
     * 月份改变事件
     *
     * @param listener listener
     */
    fun setOnMonthChangeListener(listener: OnMonthChangeListener) {
        mDelegate.mMonthChangeListener = listener
    }

    /**
     * 周视图切换监听
     *
     * @param listener listener
     */
    fun setOnWeekChangeListener(listener: OnWeekChangeListener) {
        mDelegate.mWeekChangeListener = listener
    }

    /**
     * 日期选择事件
     *
     * @param listener listener
     */
    fun setOnCalendarSelectListener(listener: OnCalendarSelectListener?) {
        mDelegate.mCalendarSelectListener = listener
        if (mDelegate.mCalendarSelectListener == null) return
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_DEFAULT) return
        if (!isInRange(mDelegate.mSelectedCalendar)) return
        mDelegate.updateSelectCalendarScheme()
    }

    /**
     * 日期选择事件
     *
     * @param listener listener
     */
    fun setOnCalendarRangeSelectListener(listener: OnCalendarRangeSelectListener) {
        mDelegate.mCalendarRangeSelectListener = listener
    }

    /**
     * 日期多选事件
     *
     * @param listener listener
     */
    fun setOnCalendarMultiSelectListener(listener: OnCalendarMultiSelectListener) {
        mDelegate.mCalendarMultiSelectListener = listener
    }

    /**
     * 设置最小范围和最大访问，default：minRange = -1，maxRange = -1 没有限制
     *
     * @param minRange minRange
     * @param maxRange maxRange
     */
    fun setSelectRange(minRange: Int, maxRange: Int) {
        if (minRange > maxRange) return
        mDelegate.setSelectRange(minRange, maxRange)
    }

    fun setSelectStartCalendar(startYear: Int, startMonth: Int, startDay: Int) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) return
        val startCalendar = Calendar()
        startCalendar.year = startYear
        startCalendar.month = startMonth
        startCalendar.day = startDay
        setSelectStartCalendar(startCalendar)
    }

    private fun setSelectStartCalendar(startCalendar: Calendar?) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) return
        if (startCalendar == null) return
        if (!isInRange(startCalendar)) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener?.onSelectOutOfRange(startCalendar, true)
            }
            return
        }
        if (onCalendarIntercept(startCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener?.onCalendarInterceptClick(startCalendar, false)
            }
            return
        }
        mDelegate.mSelectedEndRangeCalendar = null
        mDelegate.mSelectedStartRangeCalendar = startCalendar
        scrollToCalendar(startCalendar.year, startCalendar.month, startCalendar.day)
    }

    fun setSelectEndCalendar(endYear: Int, endMonth: Int, endDay: Int) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) return
        if (mDelegate.mSelectedStartRangeCalendar == null) return
        val endCalendar = Calendar()
        endCalendar.year = endYear
        endCalendar.month = endMonth
        endCalendar.day = endDay
        setSelectEndCalendar(endCalendar)
    }

    private fun setSelectEndCalendar(endCalendar: Calendar?) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) return
        if (mDelegate.mSelectedStartRangeCalendar == null) return
        setSelectCalendarRange(mDelegate.mSelectedStartRangeCalendar, endCalendar)
    }

    /**
     * 直接指定选择范围，set select calendar range
     *
     * @param startYear  startYear
     * @param startMonth startMonth
     * @param startDay   startDay
     * @param endYear    endYear
     * @param endMonth   endMonth
     * @param endDay     endDay
     */
    fun setSelectCalendarRange(
        startYear: Int, startMonth: Int, startDay: Int,
        endYear: Int, endMonth: Int, endDay: Int,
    ) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) return
        val startCalendar = Calendar()
        startCalendar.year = startYear
        startCalendar.month = startMonth
        startCalendar.day = startDay

        val endCalendar = Calendar()
        endCalendar.year = endYear
        endCalendar.month = endMonth
        endCalendar.day = endDay
        setSelectCalendarRange(startCalendar, endCalendar)
    }

    /**
     * 设置选择日期范围
     *
     * @param startCalendar startCalendar
     * @param endCalendar   endCalendar
     */
    private fun setSelectCalendarRange(startCalendar: Calendar?, endCalendar: Calendar?) {
        if (mDelegate.selectMode != CalendarViewDelegate.SELECT_MODE_RANGE) return
        if (startCalendar == null || endCalendar == null) return
        if (onCalendarIntercept(startCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener?.onCalendarInterceptClick(startCalendar, false)
            }
            return
        }
        if (onCalendarIntercept(endCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener?.onCalendarInterceptClick(endCalendar, false)
            }
            return
        }
        val minDiffer = endCalendar.differ(startCalendar)
        if (minDiffer < 0) return
        if (!isInRange(startCalendar) || !isInRange(endCalendar)) return

        //优先判断各种直接return的情况，减少代码深度
        if (mDelegate.minSelectRange != -1 && mDelegate.minSelectRange > minDiffer + 1) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener?.onSelectOutOfRange(endCalendar, true)
            }
            return
        }
        else if (mDelegate.maxSelectRange != -1 && mDelegate.maxSelectRange < minDiffer + 1) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener?.onSelectOutOfRange(endCalendar, false)
            }
            return
        }
        if (mDelegate.minSelectRange == -1 && minDiffer == 0) {
            mDelegate.mSelectedStartRangeCalendar = startCalendar
            mDelegate.mSelectedEndRangeCalendar = null
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener?.onCalendarRangeSelect(startCalendar, false)
            }
            scrollToCalendar(startCalendar.year, startCalendar.month, startCalendar.day)
            return
        }

        mDelegate.mSelectedStartRangeCalendar = startCalendar
        mDelegate.mSelectedEndRangeCalendar = endCalendar
        if (mDelegate.mCalendarRangeSelectListener != null) {
            mDelegate.mCalendarRangeSelectListener?.onCalendarRangeSelect(startCalendar, false)
            mDelegate.mCalendarRangeSelectListener?.onCalendarRangeSelect(endCalendar, true)
        }
        scrollToCalendar(startCalendar.year, startCalendar.month, startCalendar.day)
    }

    /**
     * 是否拦截日期，此设置续设置mCalendarInterceptListener
     *
     * @param calendar calendar
     * @return 是否拦截日期
     */
    private fun onCalendarIntercept(calendar: Calendar): Boolean {
        return mDelegate.mCalendarInterceptListener != null && mDelegate.mCalendarInterceptListener!!.onCalendarIntercept(calendar)
    }

    var maxMultiSelectSize: Int
        /**
         * 获得最大多选数量
         *
         * @return 获得最大多选数量
         */
        get() = mDelegate.maxMultiSelectSize
        /**
         * 设置最大多选数量
         *
         * @param maxMultiSelectSize 最大多选数量
         */
        set(maxMultiSelectSize) {
            mDelegate.maxMultiSelectSize = maxMultiSelectSize
        }

    val minSelectRange: Int
        /**
         * 最小选择范围
         *
         * @return 最小选择范围
         */
        get() = mDelegate.minSelectRange

    val maxSelectRange: Int
        /**
         * 最大选择范围
         *
         * @return 最大选择范围
         */
        get() = mDelegate.maxSelectRange

    /**
     * 日期长按事件
     *
     * @param listener listener
     */
    fun setOnCalendarLongClickListener(listener: OnCalendarLongClickListener) {
        mDelegate.mCalendarLongClickListener = listener
    }

    /**
     * 日期长按事件
     *
     * @param preventLongPressedSelect 防止长按选择日期
     * @param listener                 listener
     */
    fun setOnCalendarLongClickListener(listener: OnCalendarLongClickListener, preventLongPressedSelect: Boolean) {
        mDelegate.mCalendarLongClickListener = listener
        mDelegate.isPreventLongPressedSelected = preventLongPressedSelect
    }

    /**
     * 视图改变事件
     *
     * @param listener listener
     */
    fun setOnViewChangeListener(listener: OnViewChangeListener) {
        mDelegate.mViewChangeListener = listener
    }

    fun setOnYearViewChangeListener(listener: OnYearViewChangeListener) {
        mDelegate.mYearViewChangeListener = listener
    }

    /**
     * 保持状态
     *
     * @return 状态
     */
    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        val parcelable = super.onSaveInstanceState()
        bundle.putParcelable("super", parcelable)
        bundle.putSerializable("selected_calendar", mDelegate.mSelectedCalendar)
        bundle.putSerializable("index_calendar", mDelegate.mIndexCalendar)
        return bundle
    }

    /**
     * 恢复状态
     *
     * @param state 状态
     */
    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        val superData = bundle.getParcelable<Parcelable>("super")
        mDelegate.mSelectedCalendar = bundle.getSerializable("selected_calendar") as Calendar
        mDelegate.mIndexCalendar = bundle.getSerializable("index_calendar") as Calendar
        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener?.onCalendarSelect(mDelegate.mSelectedCalendar, false)
        }
        scrollToCalendar(
            mDelegate.mIndexCalendar.year,
            mDelegate.mIndexCalendar.month,
            mDelegate.mIndexCalendar.day
        )
        update()
        super.onRestoreInstanceState(superData)
    }

    /**
     * 初始化时初始化日历卡默认选择位置
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (parent != null && parent is CalendarLayout) {
            mParentLayout = parent as CalendarLayout
            monthViewPager?.mParentLayout = mParentLayout
            weekViewPager?.mParentLayout = mParentLayout
            mParentLayout?.mWeekBar = mWeekBar
            mParentLayout?.setup(mDelegate)
            mParentLayout?.initStatus()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (!mDelegate.isFullScreenCalendar) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        setCalendarItemHeight((height - mDelegate.weekBarHeight) / 6)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * 标记哪些日期有事件
     *
     * @param mSchemeDates mSchemeDatesMap 通过自己的需求转换即可
     */
    fun setSchemeDate(mSchemeDates: MutableMap<String, Calendar>) {
        mDelegate.mSchemeDatesMap = mSchemeDates
        mDelegate.updateSelectCalendarScheme()
        mYearViewPager?.update()
        monthViewPager?.updateScheme()
        weekViewPager?.updateScheme()
    }

    /**
     * 清空日期标记
     */
    fun clearSchemeDate() {
        mDelegate.mSchemeDatesMap = null
        mDelegate.clearSelectedScheme()
        mYearViewPager?.update()
        monthViewPager?.updateScheme()
        weekViewPager?.updateScheme()
    }

    /**
     * 添加事物标记
     *
     * @param calendar calendar
     */
    fun addSchemeDate(calendar: Calendar?) {
        if (calendar == null || !calendar.isAvailable) return
        if (mDelegate.mSchemeDatesMap == null) mDelegate.mSchemeDatesMap = HashMap()
        mDelegate.mSchemeDatesMap?.remove(calendar.toString())
        mDelegate.mSchemeDatesMap!![calendar.toString()] = calendar
        mDelegate.updateSelectCalendarScheme()
        mYearViewPager?.update()
        monthViewPager?.updateScheme()
        weekViewPager?.updateScheme()
    }

    /**
     * 添加事物标记
     *
     * @param mSchemeDates mSchemeDates
     */
    fun addSchemeDate(mSchemeDates: MutableMap<String, Calendar>) {
        if (mSchemeDates.isEmpty()) return
        if (mDelegate.mSchemeDatesMap == null) mDelegate.mSchemeDatesMap = HashMap()
        mDelegate.addSchemes(mSchemeDates)
        mDelegate.updateSelectCalendarScheme()
        mYearViewPager?.update()
        monthViewPager?.updateScheme()
        weekViewPager?.updateScheme()
    }

    /**
     * 移除某天的标记
     * 这个API是安全的
     *
     * @param calendar calendar
     */
    fun removeSchemeDate(calendar: Calendar?) {
        if (calendar == null) return
        if (mDelegate.mSchemeDatesMap == null || mDelegate.mSchemeDatesMap!!.isEmpty()) return
        mDelegate.mSchemeDatesMap?.remove(calendar.toString())
        if (mDelegate.mSelectedCalendar == calendar) mDelegate.clearSelectedScheme()

        mYearViewPager?.update()
        monthViewPager?.updateScheme()
        weekViewPager?.updateScheme()
    }

    /**
     * 设置背景色
     *
     * @param yearViewBackground 年份卡片的背景色
     * @param weekBackground     星期栏背景色
     * @param lineBg             线的颜色
     */
    fun setBackground(yearViewBackground: Int, weekBackground: Int, lineBg: Int) {
        mWeekBar?.setBackgroundColor(weekBackground)
        mYearViewPager?.setBackgroundColor(yearViewBackground)
        mWeekLine?.setBackgroundColor(lineBg)
    }

    /**
     * 设置文本颜色
     *
     * @param currentDayTextColor      今天字体颜色
     * @param curMonthTextColor        当前月份字体颜色
     * @param otherMonthColor          其它月份字体颜色
     * @param curMonthLunarTextColor   当前月份农历字体颜色
     * @param otherMonthLunarTextColor 其它农历字体颜色
     */
    fun setTextColor(
        currentDayTextColor: Int,
        curMonthTextColor: Int,
        otherMonthColor: Int,
        curMonthLunarTextColor: Int,
        otherMonthLunarTextColor: Int,
    ) {
        if (monthViewPager == null || weekViewPager == null) return
        mDelegate.setTextColor(
            currentDayTextColor, curMonthTextColor,
            otherMonthColor, curMonthLunarTextColor, otherMonthLunarTextColor
        )
        monthViewPager?.updateStyle()
        weekViewPager?.updateStyle()
    }

    /**
     * 设置选择的效果
     *
     * @param selectedThemeColor     选中的标记颜色
     * @param selectedTextColor      选中的字体颜色
     * @param selectedLunarTextColor 选中的农历字体颜色
     */
    fun setSelectedColor(selectedThemeColor: Int, selectedTextColor: Int, selectedLunarTextColor: Int) {
        if (monthViewPager == null || weekViewPager == null) return
        mDelegate.setSelectColor(selectedThemeColor, selectedTextColor, selectedLunarTextColor)
        monthViewPager?.updateStyle()
        weekViewPager?.updateStyle()
    }

    /**
     * 定制颜色
     *
     * @param selectedThemeColor 选中的标记颜色
     * @param schemeColor        标记背景色
     */
    fun setThemeColor(selectedThemeColor: Int, schemeColor: Int) {
        if (monthViewPager == null || weekViewPager == null) return
        mDelegate.setThemeColor(selectedThemeColor, schemeColor)
        monthViewPager?.updateStyle()
        weekViewPager?.updateStyle()
    }

    /**
     * 设置标记的色
     *
     * @param schemeLunarTextColor 标记农历颜色
     * @param schemeColor          标记背景色
     * @param schemeTextColor      标记字体颜色
     */
    fun setSchemeColor(schemeColor: Int, schemeTextColor: Int, schemeLunarTextColor: Int) {
        if (monthViewPager == null || weekViewPager == null) return
        mDelegate.setSchemeColor(schemeColor, schemeTextColor, schemeLunarTextColor)
        monthViewPager?.updateStyle()
        weekViewPager?.updateStyle()
    }

    /**
     * 设置年视图的颜色
     *
     * @param yearViewMonthTextColor 年视图月份颜色
     * @param yearViewDayTextColor   年视图天的颜色
     * @param yarViewSchemeTextColor 年视图标记颜色
     */
    fun setYearViewTextColor(yearViewMonthTextColor: Int, yearViewDayTextColor: Int, yarViewSchemeTextColor: Int) {
        if (mYearViewPager == null) return
        mDelegate.setYearViewTextColor(yearViewMonthTextColor, yearViewDayTextColor, yarViewSchemeTextColor)
        mYearViewPager?.updateStyle()
    }

    /**
     * 设置星期栏的背景和字体颜色
     *
     * @param weekBackground 背景色
     * @param weekTextColor  字体颜色
     */
    fun setWeeColor(weekBackground: Int, weekTextColor: Int) {
        if (mWeekBar == null) return
        mWeekBar?.setBackgroundColor(weekBackground)
        mWeekBar?.setTextColor(weekTextColor)
    }

    fun setCalendarPadding(mCalendarPadding: Int) {
        mDelegate.calendarPadding = mCalendarPadding
        update()
    }

    fun setCalendarPaddingLeft(mCalendarPaddingLeft: Int) {
        mDelegate.calendarPaddingLeft = mCalendarPaddingLeft
        update()
    }

    fun setCalendarPaddingRight(mCalendarPaddingRight: Int) {
        mDelegate.calendarPaddingRight = mCalendarPaddingRight
        update()
    }

    /**
     * 默认选择模式
     */
    fun setSelectDefaultMode() {
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) return
        mDelegate.mSelectedCalendar = mDelegate.mIndexCalendar
        mDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_DEFAULT
        mWeekBar?.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.weekStart, false)
        monthViewPager?.updateDefaultSelect()
        weekViewPager?.updateDefaultSelect()
    }

    /**
     * 范围模式
     */
    fun setSelectRangeMode() {
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_RANGE) return
        mDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_RANGE
        clearSelectRange()
    }

    /**
     * 多选模式
     */
    fun setSelectMultiMode() {
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_MULTI) return
        mDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_MULTI
        clearMultiSelect()
    }

    /**
     * 单选模式
     */
    fun setSelectSingleMode() {
        if (mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_SINGLE) return
        mDelegate.selectMode = CalendarViewDelegate.SELECT_MODE_SINGLE
        weekViewPager?.updateSelected()
        monthViewPager?.updateSelected()
    }

    /**
     * 设置星期日周起始
     */
    fun setWeekStarWithSun() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_SUN)
    }

    /**
     * 设置星期一周起始
     */
    fun setWeekStarWithMon() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_MON)
    }

    /**
     * 设置星期六周起始
     */
    fun setWeekStarWithSat() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_SAT)
    }

    /**
     * 设置周起始
     * CalendarViewDelegate.WEEK_START_WITH_SUN
     * CalendarViewDelegate.WEEK_START_WITH_MON
     * CalendarViewDelegate.WEEK_START_WITH_SAT
     *
     * @param weekStart 周起始
     */
    private fun setWeekStart(weekStart: Int) {
        if (weekStart != CalendarViewDelegate.WEEK_START_WITH_SUN && weekStart != CalendarViewDelegate.WEEK_START_WITH_MON && weekStart != CalendarViewDelegate.WEEK_START_WITH_SAT) return
        if (weekStart == mDelegate.weekStart) return
        mDelegate.weekStart = weekStart
        mWeekBar?.onWeekStartChange(weekStart)
        mWeekBar?.onDateSelected(mDelegate.mSelectedCalendar, weekStart, false)
        weekViewPager?.updateWeekStart()
        monthViewPager?.updateWeekStart()
        mYearViewPager?.updateWeekStart()
    }

    val isSingleSelectMode: Boolean
        /**
         * 是否是单选模式
         *
         * @return isSingleSelectMode
         */
        get() = mDelegate.selectMode == CalendarViewDelegate.SELECT_MODE_SINGLE

    /**
     * 设置显示模式为全部
     */
    fun setAllMode() {
        setShowMode(CalendarViewDelegate.MODE_ALL_MONTH)
    }

    /**
     * 设置显示模式为仅当前月份
     */
    fun setOnlyCurrentMode() {
        setShowMode(CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH)
    }

    /**
     * 设置显示模式为填充
     */
    fun setFixMode() {
        setShowMode(CalendarViewDelegate.MODE_FIT_MONTH)
    }

    /**
     * 设置显示模式
     * CalendarViewDelegate.MODE_ALL_MONTH
     * CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH
     * CalendarViewDelegate.MODE_FIT_MONTH
     *
     * @param mode 月视图显示模式
     */
    private fun setShowMode(mode: Int) {
        if (mode != CalendarViewDelegate.MODE_ALL_MONTH && mode != CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH && mode != CalendarViewDelegate.MODE_FIT_MONTH) return
        if (mDelegate.monthViewShowMode == mode) return
        mDelegate.monthViewShowMode = mode
        weekViewPager?.updateShowMode()
        monthViewPager?.updateShowMode()
        weekViewPager?.notifyDataSetChanged()
    }

    /**
     * 更新界面，
     * 重新设置颜色等都需要调用该方法
     */
    private fun update() {
        mWeekBar?.onWeekStartChange(mDelegate.weekStart)
        mYearViewPager?.update()
        monthViewPager?.updateScheme()
        weekViewPager?.updateScheme()
    }

    /**
     * 更新周视图
     */
    fun updateWeekBar() {
        mWeekBar?.onWeekStartChange(mDelegate.weekStart)
    }

    /**
     * 更新当前日期
     */
    fun updateCurrentDate() {
        if (monthViewPager == null || weekViewPager == null) return
        val calendar = java.util.Calendar.getInstance()
        val day = calendar[java.util.Calendar.DAY_OF_MONTH]
        if (curDay == day) return
        mDelegate.updateCurrentDay()
        monthViewPager?.updateCurrentDate()
        weekViewPager?.updateCurrentDate()
    }

    val currentWeekCalendars: List<Calendar>
        /**
         * 获取当前周数据
         *
         * @return 获取当前周数据
         */
        get() = weekViewPager!!.currentWeekCalendars

    val currentMonthCalendars: List<Calendar>
        /**
         * 获取当前月份日期
         *
         * @return return
         */
        get() = monthViewPager?.currentMonthCalendars!!

    val selectedCalendar: Calendar
        /**
         * 获取选择的日期
         *
         * @return 获取选择的日期
         */
        get() = mDelegate.mSelectedCalendar

    val minRangeCalendar: Calendar
        /**
         * 获得最小范围日期
         *
         * @return 最小范围日期
         */
        get() = mDelegate.minRangeCalendar

    val maxRangeCalendar: Calendar
        /**
         * 获得最大范围日期
         *
         * @return 最大范围日期
         */
        get() = mDelegate.maxRangeCalendar

    /**
     * 是否在日期范围内
     *
     * @param calendar calendar
     * @return 是否在日期范围内
     */
    private fun isInRange(calendar: Calendar): Boolean {
        return CalendarUtil.isCalendarInRange(calendar, mDelegate)
    }

    /**
     * 年份视图切换事件，快速年份切换
     */
    interface OnYearChangeListener {
        fun onYearChange(year: Int)
    }

    /**
     * 月份切换事件
     */
    interface OnMonthChangeListener {
        fun onMonthChange(year: Int, month: Int)
    }

    /**
     * 周视图切换事件
     */
    interface OnWeekChangeListener {
        fun onWeekChange(weekCalendars: List<Calendar>)
    }

    /**
     * 内部日期选择，不暴露外部使用
     * 主要是用于更新日历CalendarLayout位置
     */
    interface OnInnerDateSelectedListener {
        /**
         * 月视图点击
         *
         * @param calendar calendar
         * @param isClick  是否是点击
         */
        fun onMonthDateSelected(calendar: Calendar, isClick: Boolean)

        /**
         * 周视图点击
         *
         * @param calendar calendar
         * @param isClick  是否是点击
         */
        fun onWeekDateSelected(calendar: Calendar, isClick: Boolean)
    }

    /**
     * 日历范围选择事件
     */
    interface OnCalendarRangeSelectListener {
        /**
         * 范围选择超出范围越界
         *
         * @param calendar calendar
         */
        fun onCalendarSelectOutOfRange(calendar: Calendar)

        /**
         * 选择范围超出范围
         *
         * @param calendar        calendar
         * @param isOutOfMinRange 是否小于最小范围，否则为最大范围
         */
        fun onSelectOutOfRange(calendar: Calendar, isOutOfMinRange: Boolean)

        /**
         * 日期选择事件
         *
         * @param calendar calendar
         * @param isEnd    是否结束
         */
        fun onCalendarRangeSelect(calendar: Calendar, isEnd: Boolean)
    }

    /**
     * 日历多选事件
     */
    interface OnCalendarMultiSelectListener {
        /**
         * 多选超出范围越界
         *
         * @param calendar calendar
         */
        fun onCalendarMultiSelectOutOfRange(calendar: Calendar)

        /**
         * 多选超出大小
         *
         * @param maxSize  最大大小
         * @param calendar calendar
         */
        fun onMultiSelectOutOfSize(calendar: Calendar, maxSize: Int)

        /**
         * 多选事件
         *
         * @param calendar calendar
         * @param curSize  curSize
         * @param maxSize  maxSize
         */
        fun onCalendarMultiSelect(calendar: Calendar, curSize: Int, maxSize: Int)
    }

    /**
     * 日历选择事件
     */
    interface OnCalendarSelectListener {
        /**
         * 超出范围越界
         *
         * @param calendar calendar
         */
        fun onCalendarOutOfRange(calendar: Calendar)

        /**
         * 日期选择事件
         *
         * @param calendar calendar
         * @param isClick  isClick
         */
        fun onCalendarSelect(calendar: Calendar, isClick: Boolean)
    }

    interface OnCalendarLongClickListener {
        /**
         * 超出范围越界
         *
         * @param calendar calendar
         */
        fun onCalendarLongClickOutOfRange(calendar: Calendar)

        /**
         * 日期长按事件
         *
         * @param calendar calendar
         */
        fun onCalendarLongClick(calendar: Calendar)
    }

    /**
     * 视图改变事件
     */
    interface OnViewChangeListener {
        /**
         * 视图改变事件
         *
         * @param isMonthView isMonthView是否是月视图
         */
        fun onViewChange(isMonthView: Boolean)
    }

    /**
     * 年视图改变事件
     */
    interface OnYearViewChangeListener {
        /**
         * 年视图变化
         *
         * @param isClose 是否关闭
         */
        fun onYearViewChange(isClose: Boolean)
    }

    /**
     * 拦截日期是否可用事件
     */
    interface OnCalendarInterceptListener {
        fun onCalendarIntercept(calendar: Calendar): Boolean

        fun onCalendarInterceptClick(calendar: Calendar, isClick: Boolean)
    }

    /**
     * 点击Padding位置事件
     */
    interface OnClickCalendarPaddingListener {
        /**
         * 点击Padding位置的事件
         *
         * @param x                x坐标
         * @param y                y坐标
         * @param isMonthView      是否是月视图，不是则为周视图
         * @param adjacentCalendar 相邻的日历日期
         * @param obj              此处的对象，自行设置
         */
        fun onClickCalendarPadding(
            x: Float, y: Float, isMonthView: Boolean,
            adjacentCalendar: Calendar, obj: Any?,
        )
    }
}
