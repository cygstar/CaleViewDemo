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
package com.cygnet.caleviewdemo.view.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.cygnet.caleviewdemo.R
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.YearView
import kotlin.math.min

/**
 * 自定义年视图
 * Created by huanghaibin on 2018/10/9.
 */
class CustomYearView(context: Context) : YearView(context) {

    private val mTextPadding: Int = dipToPx(context, 3f)

    /**
     * 闰年字体
     */
    private val mLeapYearTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        mLeapYearTextPaint.textSize = dipToPx(context, 12f).toFloat()
        mLeapYearTextPaint.color = -0x2e2e2f
        mLeapYearTextPaint.isAntiAlias = true
        mLeapYearTextPaint.isFakeBoldText = true
    }

    override fun onDrawMonth(canvas: Canvas, year: Int, month: Int, x: Int, y: Int, width: Int, height: Int) {
        val text = context.resources.getStringArray(R.array.month_string_array)[month - 1]
        canvas.drawText(text, (x + mItemWidth / 2 - mTextPadding).toFloat(), y + mMonthTextBaseLine, mMonthTextPaint)
        if (month == 2 && isLeapYear(year)) {
            val w = getTextWidth(mMonthTextPaint, text)
            canvas.drawText("闰年", x + mItemWidth / 2 - mTextPadding + w + dipToPx(context, 6f), y + mMonthTextBaseLine, mLeapYearTextPaint)
        }
    }

    private fun getTextWidth(paint: Paint, text: String): Float {
        return paint.measureText(text)
    }

    override fun onDrawWeek(canvas: Canvas, week: Int, x: Int, y: Int, width: Int, height: Int) {
        val text = context.resources.getStringArray(R.array.year_view_week_string_array)[week]
        canvas.drawText(text, (x + width / 2).toFloat(), y + mWeekTextBaseLine, mWeekTextPaint)
    }

    override fun onDrawSelected(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean): Boolean {
        val cx = x + mItemWidth / 2
        val cy = y + mItemHeight / 2
        val radius = (min(mItemWidth.toDouble(), mItemHeight.toDouble()) / 8 * 5).toInt()
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), mSelectedPaint)
        return true
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {}

    override fun onDrawText(canvas: Canvas, calendar: Calendar, x: Int, y: Int, hasScheme: Boolean, isSelected: Boolean) {
        val baselineY = mTextBaseLine + y
        val cx = x + mItemWidth / 2
        when {
            isSelected -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (hasScheme) mSchemeTextPaint else mSelectTextPaint)
            hasScheme -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else mSchemeTextPaint)
            else -> canvas.drawText(calendar.day.toString(), cx.toFloat(), baselineY, if (calendar.isCurrentDay) mCurDayTextPaint else mCurMonthTextPaint)
        }
    }

    companion object {
        /**
         * 是否是闰年
         *
         * @param year year
         * @return 是否是闰年
         */
        private fun isLeapYear(year: Int): Boolean {
            return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)
        }

        /**
         * dp转px
         *
         * @param context context
         * @param dp dp
         * @return px
         */
        private fun dipToPx(context: Context, dp: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }
    }
}
