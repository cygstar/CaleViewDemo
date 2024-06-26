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
import android.graphics.Canvas
import com.cygnet.caleviewdemo.R
import com.cygnet.caleviewdemo.calendar.CalendarUtil.dipToPx

/**
 * 默认年视图
 * Created by huanghaibin on 2018/10/9.
 */
class DefaultYearView(context: Context) : YearView(context) {
    private val mTextPadding = dipToPx(context, 3f)

    override fun onDrawMonth(canvas: Canvas, year: Int, month: Int, x: Int, y: Int, width: Int, height: Int) {
        val text = context.resources.getStringArray(R.array.month_string_array)[month - 1]
        canvas.drawText(text, (x + mItemWidth / 2 - mTextPadding).toFloat(), y + mMonthTextBaseLine, mMonthTextPaint)
    }

    override fun onDrawWeek(canvas: Canvas, week: Int, x: Int, y: Int, width: Int, height: Int) {
        val text = context.resources.getStringArray(R.array.year_view_week_string_array)[week]
        canvas.drawText(text, (x + width / 2).toFloat(), y + mWeekTextBaseLine, mWeekTextPaint)
    }

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        return false
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {}

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2

        when {
            isSelected -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (hasScheme) mSchemeTextPaint else mSelectTextPaint)
            }
            hasScheme -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint)
            }
            else -> {
                canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mCurMonthTextPaint else mOtherMonthTextPaint)
            }
        }
    }
}
