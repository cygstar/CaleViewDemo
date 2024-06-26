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
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getFirstCalendarStartWithMinCalendar
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getWeekCalendars
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getWeekCountBetweenBothCalendar
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getWeekFromCalendarStartWithMinCalendar
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getWeekFromDayInMonth
import com.cygnet.caleviewdemo.calendar.LunarCalendar.setupLunarCalendar

/**
 * 周视图滑动ViewPager，需要动态固定高度
 * 周视图是连续不断的视图，因此不能简单的得出每年都有52+1周，这样会计算重叠的部分
 * WeekViewPager需要和CalendarView关联:
 */
class WeekViewPager(context: Context?, attrs: AttributeSet? = null) : ViewPager(context!!, attrs) {

    private var mDelegate: CalendarViewDelegate? = null
    private var isUpdateWeekView = false
    private var mWeekCount = 0

    /**
     * 日历布局，需要在日历下方放自己的布局
     */
    var mParentLayout: CalendarLayout? = null

    /**
     * 是否使用滚动到某一天
     */
    private var isUsingScrollToCalendar = false

    fun setup(delegate: CalendarViewDelegate?) {
        this.mDelegate = delegate
        init()
    }

    private fun init() {
        mWeekCount = getWeekCountBetweenBothCalendar(
            mDelegate!!.minYear,
            mDelegate!!.minYearMonth,
            mDelegate!!.minYearDay,
            mDelegate!!.maxYear,
            mDelegate!!.maxYearMonth,
            mDelegate!!.maxYearDay,
            mDelegate!!.weekStart
        )
        adapter = WeekViewPagerAdapter()
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                //默认的显示星期四，周视图切换就显示星期4
                if (visibility != VISIBLE) {
                    isUsingScrollToCalendar = false
                    return
                }
                if (isUsingScrollToCalendar) {
                    isUsingScrollToCalendar = false
                    return
                }
                val view = findViewWithTag<BaseWeekView>(position)
                if (view != null) {
                    view.performClickCalendar(if (mDelegate!!.selectMode != CalendarViewDelegate.SELECT_MODE_DEFAULT) mDelegate!!.mIndexCalendar else mDelegate!!.mSelectedCalendar, !isUsingScrollToCalendar)
                    if (mDelegate!!.mWeekChangeListener != null) mDelegate!!.mWeekChangeListener!!.onWeekChange(currentWeekCalendars)
                }
                isUsingScrollToCalendar = false
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    val currentWeekCalendars: List<Calendar>
        /**
         * 获取当前周数据
         *
         * @return 获取当前周数据
         */
        get() {
            val calendars: List<Calendar> = getWeekCalendars(
                mDelegate!!.mIndexCalendar,
                mDelegate!!
            )
            mDelegate!!.addSchemesFromMap(calendars)
            return calendars
        }

    /**
     * 更新周视图
     */
    fun notifyDataSetChanged() {
        mWeekCount = getWeekCountBetweenBothCalendar(
            mDelegate!!.minYear,
            mDelegate!!.minYearMonth,
            mDelegate!!.minYearDay,
            mDelegate!!.maxYear,
            mDelegate!!.maxYearMonth,
            mDelegate!!.maxYearDay,
            mDelegate!!.weekStart
        )
        notifyAdapterDataSetChanged()
    }

    /**
     * 更新周视图布局
     */
    fun updateWeekViewClass() {
        isUpdateWeekView = true
        notifyAdapterDataSetChanged()
        isUpdateWeekView = false
    }

    /**
     * 更新日期范围
     */
    fun updateRange() {
        isUpdateWeekView = true
        notifyDataSetChanged()
        isUpdateWeekView = false
        if (visibility != VISIBLE) return
        isUsingScrollToCalendar = true
        val calendar = mDelegate!!.mSelectedCalendar
        updateSelected(calendar, false)
        if (mDelegate!!.mInnerListener != null) mDelegate!!.mInnerListener!!.onWeekDateSelected(calendar, false)
        if (mDelegate!!.mCalendarSelectListener != null) mDelegate!!.mCalendarSelectListener!!.onCalendarSelect(calendar, false)
        mParentLayout!!.updateSelectWeek(getWeekFromDayInMonth(calendar, mDelegate!!.weekStart))
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
        updateSelected(calendar, smoothScroll)
        if (mDelegate!!.mInnerListener != null) mDelegate!!.mInnerListener!!.onWeekDateSelected(calendar, false)
        if (mDelegate!!.mCalendarSelectListener != null && invokeListener) mDelegate!!.mCalendarSelectListener!!.onCalendarSelect(calendar, false)
        mParentLayout!!.updateSelectWeek(getWeekFromDayInMonth(calendar, mDelegate!!.weekStart))
    }

    /**
     * 滚动到当前
     */
    fun scrollToCurrent(smoothScroll: Boolean) {
        isUsingScrollToCalendar = true
        val position = getWeekFromCalendarStartWithMinCalendar(
            mDelegate!!.currentDay,
            mDelegate!!.minYear,
            mDelegate!!.minYearMonth,
            mDelegate!!.minYearDay,
            mDelegate!!.weekStart
        ) - 1
        val curItem = currentItem
        if (curItem == position) isUsingScrollToCalendar = false
        setCurrentItem(position, smoothScroll)
        val view = findViewWithTag<BaseWeekView>(position)
        if (view != null) {
            view.performClickCalendar(mDelegate!!.currentDay, false)
            view.setSelectedCalendar(mDelegate!!.currentDay)
            view.invalidate()
        }

        if (mDelegate!!.mCalendarSelectListener != null && visibility == VISIBLE) {
            mDelegate!!.mCalendarSelectListener!!.onCalendarSelect(mDelegate!!.mSelectedCalendar, false)
        }

        if (visibility == VISIBLE) mDelegate!!.mInnerListener!!.onWeekDateSelected(mDelegate!!.currentDay, false)
        mParentLayout!!.updateSelectWeek(getWeekFromDayInMonth(mDelegate!!.currentDay, mDelegate!!.weekStart))
    }

    /**
     * 更新任意一个选择的日期
     */
    fun updateSelected(calendar: Calendar, smoothScroll: Boolean) {
        val position = getWeekFromCalendarStartWithMinCalendar(
            calendar,
            mDelegate!!.minYear,
            mDelegate!!.minYearMonth,
            mDelegate!!.minYearDay,
            mDelegate!!.weekStart
        ) - 1
        val curItem = currentItem
        isUsingScrollToCalendar = curItem != position
        setCurrentItem(position, smoothScroll)
        val view = findViewWithTag<BaseWeekView>(position)
        if (view != null) {
            view.setSelectedCalendar(calendar)
            view.invalidate()
        }
    }

    /**
     * 更新单选模式
     */
    fun updateSingleSelect() {
        if (mDelegate!!.selectMode == CalendarViewDelegate.SELECT_MODE_DEFAULT) return
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateSingleSelect()
        }
    }

    /**
     * 更新为默认选择模式
     */
    fun updateDefaultSelect() {
        val view = findViewWithTag<BaseWeekView>(currentItem)
        if (view != null) {
            view.setSelectedCalendar(mDelegate!!.mSelectedCalendar)
            view.invalidate()
        }
    }

    /**
     * 更新选择效果
     */
    fun updateSelected() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.setSelectedCalendar(mDelegate!!.mSelectedCalendar)
            view.invalidate()
        }
    }

    /**
     * 更新字体颜色大小
     */
    fun updateStyle() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateStyle()
            view.invalidate()
        }
    }

    /**
     * 更新标记日期
     */
    fun updateScheme() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.update()
        }
    }

    /**
     * 更新当前日期，夜间过度的时候调用这个函数，一般不需要调用
     */
    fun updateCurrentDate() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateCurrentDate()
        }
    }

    /**
     * 更新显示模式
     */
    fun updateShowMode() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateShowMode()
        }
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        if (adapter == null) return
        val count = adapter!!.count
        mWeekCount = getWeekCountBetweenBothCalendar(
            mDelegate!!.minYear,
            mDelegate!!.minYearMonth,
            mDelegate!!.minYearDay,
            mDelegate!!.maxYear,
            mDelegate!!.maxYearMonth,
            mDelegate!!.maxYearDay,
            mDelegate!!.weekStart
        )
        /*
         * 如果count发生变化，意味着数据源变化，则必须先调用notifyDataSetChanged()，
         * 否则会抛出异常
         */
        if (count != mWeekCount) {
            isUpdateWeekView = true
            adapter!!.notifyDataSetChanged()
        }
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateWeekStart()
        }
        isUpdateWeekView = false
        updateSelected(mDelegate!!.mSelectedCalendar, false)
    }

    /**
     * 更新高度
     */
    fun updateItemHeight() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.updateItemHeight()
            view.requestLayout()
        }
    }

    /**
     * 清除选择范围
     */
    fun clearSelectRange() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.invalidate()
        }
    }

    fun clearSingleSelect() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.mCurrentItem = -1
            view.invalidate()
        }
    }

    fun clearMultiSelect() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as BaseWeekView
            view.mCurrentItem = -1
            view.invalidate()
        }
    }

    private fun notifyAdapterDataSetChanged() {
        if (adapter == null) return
        adapter!!.notifyDataSetChanged()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return mDelegate!!.isWeekViewScrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mDelegate!!.isWeekViewScrollable && super.onInterceptTouchEvent(ev)
    }

    /**
     * 周视图的高度应该与日历项的高度一致
     */
    override fun onMeasure(width: Int, height: Int) {
        var heightMeasureSpec = height
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mDelegate!!.calendarItemHeight, MeasureSpec.EXACTLY)
        super.onMeasure(width, heightMeasureSpec)
    }

    /**
     * 周视图切换
     */
    private inner class WeekViewPagerAdapter : PagerAdapter() {
        override fun getCount(): Int {
            return mWeekCount
        }

        override fun getItemPosition(obj: Any): Int {
            return if (isUpdateWeekView) POSITION_NONE else super.getItemPosition(obj)
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val calendar = getFirstCalendarStartWithMinCalendar(
                mDelegate!!.minYear,
                mDelegate!!.minYearMonth,
                mDelegate!!.minYearDay,
                position + 1,
                mDelegate!!.weekStart
            )
            val view: BaseWeekView
            try {
                val constructor = mDelegate!!.weekViewClass!!.getConstructor(Context::class.java)
                view = constructor.newInstance(context) as BaseWeekView
            }
            catch (_: Exception) {
                return DefaultWeekView(context)
            }
            view.mParentLayout = mParentLayout
            view.setup(mDelegate!!)
            view.setup(calendar)
            view.tag = position
            view.setSelectedCalendar(mDelegate!!.mSelectedCalendar)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            val view = obj as BaseWeekView
            view.onDestroy()
            container.removeView(view)
        }
    }
}
