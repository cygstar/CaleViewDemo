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
import android.view.WindowManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cygnet.caleviewdemo.calendar.YearRecyclerView.OnMonthSelectedListener
import kotlin.math.abs

/**
 * 年份+月份选择布局
 * ViewPager + RecyclerView
 */
class YearViewPager(context: Context, attrs: AttributeSet? = null) : ViewPager(context, attrs) {

    private lateinit var mDelegate: CalendarViewDelegate

    private var mListener: OnMonthSelectedListener? = null
    private var isUpdateYearView = false
    private var mYearCount = 0

    fun setup(delegate: CalendarViewDelegate) {
        this.mDelegate = delegate
        this.mYearCount = mDelegate.maxYear - mDelegate.minYear + 1
        adapter = object : PagerAdapter() {
            override fun getCount(): Int {
                return mYearCount
            }

            override fun getItemPosition(obj: Any): Int {
                return if (isUpdateYearView) POSITION_NONE else super.getItemPosition(obj)
            }

            override fun isViewFromObject(view: View, obj: Any): Boolean {
                return view === obj
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = YearRecyclerView(context)
                container.addView(view)
                view.setup(mDelegate)
                view.setOnMonthSelectedListener(mListener)
                view.init(position + mDelegate.minYear)
                return view
            }

            override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
                container.removeView(obj as View)
            }
        }
        currentItem = mDelegate.currentDay.year - mDelegate.minYear
    }

    override fun setCurrentItem(item: Int) {
        setCurrentItem(item, false)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        if (abs((currentItem - item).toDouble()) > 1) super.setCurrentItem(item, false)
        else super.setCurrentItem(item, false)
    }

    /**
     * 通知刷新
     */
    fun notifyDataSetChanged() {
        this.mYearCount = mDelegate.maxYear - mDelegate.minYear + 1
        if (adapter != null) adapter!!.notifyDataSetChanged()
    }

    /**
     * 滚动到某年
     *
     * @param year         year
     * @param smoothScroll smoothScroll
     */
    fun scrollToYear(year: Int, smoothScroll: Boolean) {
        setCurrentItem(year - mDelegate.minYear, smoothScroll)
    }

    /**
     * 更新日期范围
     */
    fun updateRange() {
        isUpdateYearView = true
        notifyDataSetChanged()
        isUpdateYearView = false
    }

    /**
     * 更新界面
     */
    fun update() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as YearRecyclerView
            view.notifyAdapterDataSetChanged()
        }
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as YearRecyclerView
            view.updateWeekStart()
            view.notifyAdapterDataSetChanged()
        }
    }

    /**
     * 更新字体颜色大小
     */
    fun updateStyle() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as YearRecyclerView
            view.updateStyle()
        }
    }

    fun setOnMonthSelectedListener(listener: OnMonthSelectedListener?) {
        this.mListener = listener
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return mDelegate.isYearViewScrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mDelegate.isYearViewScrollable && super.onInterceptTouchEvent(ev)
    }

    companion object {
        /**
         * 计算相对高度
         *
         * @param context context
         * @param view    view
         * @return 年月视图选择器最适合的高度
         */
        private fun getHeight(context: Context, view: View): Int {
            val manager = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            val display = manager.defaultDisplay
            val h = display.height
            val location = IntArray(2)
            view.getLocationInWindow(location)
            view.getLocationOnScreen(location)
            return h - location[1]
        }
    }
}
