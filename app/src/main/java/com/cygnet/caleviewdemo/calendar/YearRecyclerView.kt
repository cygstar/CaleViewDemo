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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getMonthDaysCount
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getMonthViewStartDiff
import com.cygnet.caleviewdemo.calendar.CalendarUtil.isMonthInRange
import java.util.Calendar

/**
 * 年份布局选择View
 */
class YearRecyclerView(context: Context, attrs: AttributeSet? = null) : RecyclerView(context, attrs) {

    private var mListener: OnMonthSelectedListener? = null
    private var mDelegate: CalendarViewDelegate? = null
    private val mAdapter = YearViewAdapter(context)

    init {
        layoutManager = GridLayoutManager(context, 3)
        adapter = mAdapter
        mAdapter.setOnItemClickListener(object : BaseRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, itemId: Long) {
                if (mListener != null && mDelegate != null) {
                    val month = mAdapter.getItem(position) ?: return
                    if (!isMonthInRange(
                            month.year, month.month,
                            mDelegate!!.minYear, mDelegate!!.minYearMonth,
                            mDelegate!!.maxYear, mDelegate!!.maxYearMonth
                        )
                    ) return
                    mListener!!.onMonthSelected(month.year, month.month)
                    if (mDelegate!!.mYearViewChangeListener != null) mDelegate!!.mYearViewChangeListener!!.onYearViewChange(true)
                }
            }
        })
    }

    /**
     * 设置
     *
     * @param delegate delegate
     */
    fun setup(delegate: CalendarViewDelegate) {
        this.mDelegate = delegate
        this.mAdapter.setup(delegate)
    }

    /**
     * 初始化年视图
     *
     * @param year year
     */
    fun init(year: Int) {
        val date = Calendar.getInstance()
        for (i in 1..12) {
            date[year, i - 1] = 1
            val mDaysCount = getMonthDaysCount(year, i)
            val month = Month()
            month.diff = getMonthViewStartDiff(year, i, mDelegate!!.weekStart)
            month.count = mDaysCount
            month.month = i
            month.year = year
            mAdapter.addItem(month)
        }
    }

    /**
     * 更新周起始
     */
    fun updateWeekStart() {
        for (item in mAdapter.items) {
            item.diff = getMonthViewStartDiff(item.year, item.month, mDelegate!!.weekStart)
        }
    }

    /**
     * 更新字体颜色大小
     */
    fun updateStyle() {
        for (i in 0 until childCount) {
            val view = getChildAt(i) as YearView
            view.updateStyle()
            view.invalidate()
        }
    }

    /**
     * 月份选择事件
     *
     * @param listener listener
     */
    fun setOnMonthSelectedListener(listener: OnMonthSelectedListener?) {
        this.mListener = listener
    }

    fun notifyAdapterDataSetChanged() {
        if (adapter == null) return
        adapter!!.notifyDataSetChanged()
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        val height = MeasureSpec.getSize(heightSpec)
        val width = MeasureSpec.getSize(widthSpec)
        mAdapter.setYearViewSize(width / 3, height / 4)
    }

    fun interface OnMonthSelectedListener {
        fun onMonthSelected(year: Int, month: Int)
    }
}
