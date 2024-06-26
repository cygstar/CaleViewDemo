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
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getFirstCalendarFromMonthViewPager
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getMonthViewHeight
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getRangeEdgeCalendar
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getWeekFromDayInMonth
import com.cygnet.caleviewdemo.calendar.LunarCalendar.setupLunarCalendar
import kotlin.math.abs

/**
 * 月份切换ViewPager，自定义适应高度
 */
class MonthViewPager(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    private var mDelegate: CalendarViewDelegate? = null
    private var isUpdateMonthView = false
    private var mMonthCount = 0
    private var mNextViewHeight = 0
    private var mPreViewHeight = 0
    private var mCurrentViewHeight = 0

    var mParentLayout: CalendarLayout? = null
    var mWeekPager: WeekViewPager? = null
    var mWeekBar: WeekBar? = null

    /**
     * 是否使用滚动到某一天
     */
    private var isUsingScrollToCalendar = false

    /**
     * 初始化
     *
     * @param delegate delegate
     */
    fun setup(delegate: CalendarViewDelegate?) {
        this.mDelegate = delegate

        updateMonthViewHeight(
            mDelegate!!.currentDay.year,
            mDelegate!!.currentDay.month
        )

        val params = layoutParams
        params.height = mCurrentViewHeight
        layoutParams = params
        init()
    }

    /**
     * 初始化
     */
    private fun init() {
        mMonthCount = (12 * (mDelegate!!.maxYear - mDelegate!!.minYear) - mDelegate!!.minYearMonth) + 1 + mDelegate!!.maxYearMonth
        adapter = MonthViewPagerAdapter()
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (mDelegate!!.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) return
                val height = if (position < currentItem) { //右滑-1
                    ((mPreViewHeight) * (1 - positionOffset) + mCurrentViewHeight * positionOffset).toInt()
                }
                else { //左滑+！
                    ((mCurrentViewHeight) * (1 - positionOffset) + (mNextViewHeight) * positionOffset).toInt()
                }
                val params = layoutParams
                params.height = height
                layoutParams = params
            }

            override fun onPageSelected(position: Int) {
                val calendar = getFirstCalendarFromMonthViewPager(position, mDelegate!!)
                if (visibility == VISIBLE) {
                    if (!mDelegate!!.isShowYearSelectedLayout && calendar.year != mDelegate!!.mIndexCalendar.year && mDelegate!!.mYearChangeListener != null) {
                        mDelegate!!.mYearChangeListener!!.onYearChange(calendar.year)
                    }
                    mDelegate!!.mIndexCalendar = calendar
                }
                //月份改变事件
                if (mDelegate!!.mMonthChangeListener != null) mDelegate!!.mMonthChangeListener!!.onMonthChange(calendar.year, calendar.month)

                //周视图显示的时候就需要动态改变月视图高度
                if (mWeekPager!!.visibility == VISIBLE) {
                    updateMonthViewHeight(calendar.year, calendar.month)
                    return
                }


                if (mDelegate!!.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
                    if (!calendar.isCurrentMonth) mDelegate!!.mSelectedCalendar = calendar
                    else mDelegate!!.mSelectedCalendar = getRangeEdgeCalendar(calendar, mDelegate!!)
                    mDelegate!!.mIndexCalendar = mDelegate!!.mSelectedCalendar
                }
                else {
                    if (mDelegate!!.mSelectedStartRangeCalendar != null && mDelegate!!.mSelectedStartRangeCalendar!!.isSameMonth(mDelegate!!.mIndexCalendar)) {
                        mDelegate!!.mIndexCalendar = mDelegate!!.mSelectedStartRangeCalendar!!
                    }
                    else {
                        if (calendar.isSameMonth(mDelegate!!.mSelectedCalendar)) {
                            mDelegate!!.mIndexCalendar = mDelegate!!.mSelectedCalendar
                        }
                    }
                }

                mDelegate!!.updateSelectCalendarScheme()
                if (!isUsingScrollToCalendar && mDelegate!!.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
                    mWeekBar!!.onDateSelected(mDelegate!!.mSelectedCalendar, mDelegate!!.weekStart, false)
                    if (mDelegate!!.mCalendarSelectListener != null) {
                        mDelegate!!.mCalendarSelectListener!!.onCalendarSelect(mDelegate!!.mSelectedCalendar, false)
                    }
                }

                val view = findViewWithTag<BaseMonthView>(position)
                if (view != null) {
                    val index = view.getSelectedIndex(mDelegate!!.mIndexCalendar)
                    if (mDelegate!!.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) view.mCurrentItem = index
                    if (index >= 0 && mParentLayout != null) mParentLayout!!.updateSelectPosition(index)
                    view.invalidate()
                }
                mWeekPager!!.updateSelected(mDelegate!!.mIndexCalendar, false)
                updateMonthViewHeight(calendar.year, calendar.month)
                isUsingScrollToCalendar = false
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    /**
     * 更新月视图的高度
     *
     * @param year  year
     * @param month month
     */
    private fun updateMonthViewHeight(year: Int, month: Int) {
        if (mDelegate!!.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) { //非动态高度就不需要了
            mCurrentViewHeight = 6 * mDelegate!!.calendarItemHeight
            val params = layoutParams
            params.height = mCurrentViewHeight
            return
        }

        if (mParentLayout != null) {
            if (visibility != VISIBLE) { //如果已经显示周视图，则需要动态改变月视图高度，否则显示就有bug
                val params = layoutParams
                params.height = getMonthViewHeight(
                    year, month,
                    mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                    mDelegate!!.monthViewShowMode
                )
                layoutParams = params
            }
            mParentLayout!!.updateContentViewTranslateY()
        }
        mCurrentViewHeight = getMonthViewHeight(
            year, month,
            mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
            mDelegate!!.monthViewShowMode
        )
        if (month == 1) {
            mPreViewHeight = getMonthViewHeight(
                year - 1, 12,
                mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                mDelegate!!.monthViewShowMode
            )
            mNextViewHeight = getMonthViewHeight(
                year, 2,
                mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                mDelegate!!.monthViewShowMode
            )
        }
        else {
            mPreViewHeight = getMonthViewHeight(
                year, month - 1,
                mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                mDelegate!!.monthViewShowMode
            )
            mNextViewHeight = if (month == 12) {
                getMonthViewHeight(
                    year + 1, 1,
                    mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                    mDelegate!!.monthViewShowMode
                )
            }
            else {
                getMonthViewHeight(
                    year, month + 1,
                    mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                    mDelegate!!.monthViewShowMode
                )
            }
        }
    }

    /**
     * 刷新
     */
    fun notifyDataSetChanged() {
        mMonthCount = (12 * (mDelegate!!.maxYear - mDelegate!!.minYear) - mDelegate!!.minYearMonth) + 1 + mDelegate!!.maxYearMonth
        notifyAdapterDataSetChanged()
    }

    /**
     * 更新月视图Class
     */
    fun updateMonthViewClass() {
        isUpdateMonthView = true
        notifyAdapterDataSetChanged()
        isUpdateMonthView = false
    }

    /**
     * 更新日期范围
     */
    fun updateRange() {
        isUpdateMonthView = true
        notifyDataSetChanged()
        isUpdateMonthView = false
        if (visibility != VISIBLE) return
        isUsingScrollToCalendar = false
        val calendar = mDelegate!!.mSelectedCalendar
        val y = calendar.year - mDelegate!!.minYear
        val position = 12 * y + calendar.month - mDelegate!!.minYearMonth
        setCurrentItem(position, false)
        val view = findViewWithTag<BaseMonthView>(position)
        if (view != null) {
            view.setSelectedCalendar(mDelegate!!.mIndexCalendar)
            view.invalidate()
            if (mParentLayout != null) mParentLayout!!.updateSelectPosition(view.getSelectedIndex(mDelegate!!.mIndexCalendar))
        }
        if (mParentLayout != null) {
            val week = getWeekFromDayInMonth(calendar, mDelegate!!.weekStart)
            mParentLayout!!.updateSelectWeek(week)
        }

        if (mDelegate!!.mInnerListener != null) mDelegate!!.mInnerListener!!.onMonthDateSelected(calendar, false)
        if (mDelegate!!.mCalendarSelectListener != null) mDelegate!!.mCalendarSelectListener!!.onCalendarSelect(calendar, false)
        updateSelected()
    }

    /**
     * 滚动到指定日期
     *
     * @param year           年
     * @param month          月
     * @param day            日
     * @param invokeListener 调用日期事件
     */
    fun scrollToCalendar(year: Int, month: Int, day: Int, smoothScroll: Boolean, invokeListener: Boolean) {
        isUsingScrollToCalendar = true
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.isCurrentDay = calendar == mDelegate!!.currentDay
        setupLunarCalendar(calendar)
        mDelegate!!.mIndexCalendar = calendar
        mDelegate!!.mSelectedCalendar = calendar
        mDelegate!!.updateSelectCalendarScheme()
        val y = calendar.year - mDelegate!!.minYear
        val position = 12 * y + calendar.month - mDelegate!!.minYearMonth
        val curItem = currentItem
        if (curItem == position) isUsingScrollToCalendar = false
        setCurrentItem(position, smoothScroll)

        val view = findViewWithTag<BaseMonthView>(position)
        if (view != null) {
            view.setSelectedCalendar(mDelegate!!.mIndexCalendar)
            view.invalidate()
            if (mParentLayout != null) mParentLayout!!.updateSelectPosition(view.getSelectedIndex(mDelegate!!.mIndexCalendar))
        }
        if (mParentLayout != null) {
            val week = getWeekFromDayInMonth(calendar, mDelegate!!.weekStart)
            mParentLayout!!.updateSelectWeek(week)
        }

        if (mDelegate!!.mCalendarSelectListener != null && invokeListener) {
            mDelegate!!.mCalendarSelectListener!!.onCalendarSelect(calendar, false)
        }
        if (mDelegate!!.mInnerListener != null) {
            mDelegate!!.mInnerListener!!.onMonthDateSelected(calendar, false)
        }

        updateSelected()
    }

    /**
     * 滚动到当前日期
     */
    fun scrollToCurrent(smoothScroll: Boolean) {
        isUsingScrollToCalendar = true
        val position = 12 * (mDelegate!!.currentDay.year - mDelegate!!.minYear) + mDelegate!!.currentDay.month - mDelegate!!.minYearMonth
        val curItem = currentItem
        if (curItem == position) isUsingScrollToCalendar = false

        setCurrentItem(position, smoothScroll)

        val view = findViewWithTag<BaseMonthView>(position)
        if (view != null) {
            view.setSelectedCalendar(mDelegate!!.currentDay)
            view.invalidate()
            if (mParentLayout != null) mParentLayout!!.updateSelectPosition(view.getSelectedIndex(mDelegate!!.currentDay))
        }

        if (mDelegate!!.mCalendarSelectListener != null && visibility == VISIBLE) {
            mDelegate!!.mCalendarSelectListener!!.onCalendarSelect(mDelegate!!.mSelectedCalendar, false)
        }
    }

    val currentMonthCalendars: List<Calendar>?
        /**
         * 获取当前月份数据
         *
         * @return 获取当前月份数据
         */
        get() {
            val view = findViewWithTag<BaseMonthView>(currentItem) ?: return null
            return view.mItems
        }

    /**
     * 更新为默认选择模式
     */
    fun updateDefaultSelect() {
        val view = findViewWithTag<BaseMonthView>(currentItem)
        if (view != null) {
            val index = view.getSelectedIndex(mDelegate!!.mSelectedCalendar)
            view.mCurrentItem = index
            if (index >= 0 && mParentLayout != null) mParentLayout!!.updateSelectPosition(index)
            view.invalidate()
        }
    }

    /**
     * 更新选择效果
     */
    fun updateSelected() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.setSelectedCalendar(mDelegate!!.mSelectedCalendar)
            view.invalidate()
        }
    }

    /**
     * 更新字体颜色大小
     */
    fun updateStyle() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.updateStyle()
            view.invalidate()
        }
    }

    /**
     * 更新标记日期
     */
    fun updateScheme() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.update()
        }
    }

    /**
     * 更新当前日期，夜间过度的时候调用这个函数，一般不需要调用
     */
    fun updateCurrentDate() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.updateCurrentDate()
        }
    }

    /**
     * 更新显示模式
     */
    fun updateShowMode() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.updateShowMode()
            view.requestLayout()
        }
        if (mDelegate!!.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) {
            mCurrentViewHeight = 6 * mDelegate!!.calendarItemHeight
            mNextViewHeight = mCurrentViewHeight
            mPreViewHeight = mCurrentViewHeight
        }
        else updateMonthViewHeight(mDelegate!!.mSelectedCalendar.year, mDelegate!!.mSelectedCalendar.month)
        val params = layoutParams
        params.height = mCurrentViewHeight
        layoutParams = params
        if (mParentLayout != null) mParentLayout!!.updateContentViewTranslateY()
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.updateWeekStart()
            view.requestLayout()
        }

        updateMonthViewHeight(mDelegate!!.mSelectedCalendar.year, mDelegate!!.mSelectedCalendar.month)
        val params = layoutParams
        params.height = mCurrentViewHeight
        layoutParams = params
        if (mParentLayout != null) mParentLayout!!.updateSelectWeek(getWeekFromDayInMonth(mDelegate!!.mSelectedCalendar, mDelegate!!.weekStart))
        updateSelected()
    }

    /**
     * 更新高度
     */
    fun updateItemHeight() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.updateItemHeight()
            view.requestLayout()
        }

        val year = mDelegate!!.mIndexCalendar.year
        val month = mDelegate!!.mIndexCalendar.month
        mCurrentViewHeight = getMonthViewHeight(
            year, month,
            mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
            mDelegate!!.monthViewShowMode
        )
        if (month == 1) {
            mPreViewHeight = getMonthViewHeight(
                year - 1, 12,
                mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                mDelegate!!.monthViewShowMode
            )
            mNextViewHeight = getMonthViewHeight(
                year, 2,
                mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                mDelegate!!.monthViewShowMode
            )
        }
        else {
            mPreViewHeight = getMonthViewHeight(
                year, month - 1,
                mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                mDelegate!!.monthViewShowMode
            )
            mNextViewHeight = if (month == 12) {
                getMonthViewHeight(
                    year + 1, 1,
                    mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                    mDelegate!!.monthViewShowMode
                )
            }
            else {
                getMonthViewHeight(
                    year, month + 1,
                    mDelegate!!.calendarItemHeight, mDelegate!!.weekStart,
                    mDelegate!!.monthViewShowMode
                )
            }
        }
        val params = layoutParams
        params.height = mCurrentViewHeight
        layoutParams = params
    }

    /**
     * 清除选择范围
     */
    fun clearSelectRange() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.invalidate()
        }
    }

    /**
     * 清除单选选择
     */
    fun clearSingleSelect() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.mCurrentItem = -1
            view.invalidate()
        }
    }

    /**
     * 清除单选选择
     */
    fun clearMultiSelect() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseMonthView
            view.mCurrentItem = -1
            view.invalidate()
        }
    }

    private fun notifyAdapterDataSetChanged() {
        if (adapter == null) return
        adapter!!.notifyDataSetChanged()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return mDelegate!!.isMonthViewScrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mDelegate!!.isMonthViewScrollable && super.onInterceptTouchEvent(ev)
    }

    override fun setCurrentItem(item: Int) {
        setCurrentItem(item, true)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        if (abs((currentItem - item).toDouble()) > 1) super.setCurrentItem(item, false)
        else super.setCurrentItem(item, smoothScroll)
    }

    /**
     * 日历卡月份Adapter
     */
    private inner class MonthViewPagerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return mMonthCount
        }

        override fun getItemPosition(obj: Any): Int {
            return if (isUpdateMonthView) POSITION_NONE else super.getItemPosition(obj)
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val year = (position + mDelegate!!.minYearMonth - 1) / 12 + mDelegate!!.minYear
            val month = (position + mDelegate!!.minYearMonth - 1) % 12 + 1
            val view: BaseMonthView
            try {
                val constructor = mDelegate!!.monthViewClass!!.getConstructor(Context::class.java)
                view = constructor.newInstance(context) as BaseMonthView
            }
            catch (_: Exception) {
                return DefaultMonthView(context)
            }
            view.mMonthViewPager = this@MonthViewPager
            view.mParentLayout = mParentLayout
            view.setup(mDelegate!!)
            view.tag = position
            view.initMonthWithDate(year, month)
            view.setSelectedCalendar(mDelegate!!.mSelectedCalendar)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            val view = obj as BaseView
            view.onDestroy()
            container.removeView(view)
        }
    }
}
