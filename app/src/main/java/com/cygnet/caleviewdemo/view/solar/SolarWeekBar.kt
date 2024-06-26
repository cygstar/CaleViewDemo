package com.cygnet.caleviewdemo.view.solar

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.cygnet.caleviewdemo.R
import com.cygnet.caleviewdemo.calendar.WeekBar

/**
 * 自定义英文栏
 * Created by huanghaibin on 2017/11/30.
 */
class SolarWeekBar(context: Context) : WeekBar(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.solar_week_bar, this, true)
        setBackgroundColor(context.resources.getColor(R.color.solar_background, null))
    }

    /**
     * 当周起始发生变化，使用自定义布局需要重写这个方法，避免出问题
     *
     * @param weekStart 周起始
     */
    override fun onWeekStartChange(weekStart: Int) {
        for (i in 0 until childCount) {
            (getChildAt(i) as TextView).text = getWeekString(i, weekStart)
        }
    }

    /**
     * 或者周文本，这个方法仅供父类使用
     *
     * @param index     index
     * @param weekStart weekStart
     * @return 或者周文本
     */
    private fun getWeekString(index: Int, weekStart: Int): String {
        val weeks = context.resources.getStringArray(R.array.english_week_string_array)

        if (weekStart == 1) return weeks[index]
        if (weekStart == 2) return weeks[if (index == 6) 0 else index + 1]
        return weeks[if (index == 0) 6 else index - 1]
    }
}
