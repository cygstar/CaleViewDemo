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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 一些日期辅助计算工具
 */
object CalendarUtil {

    private const val ONE_DAY = (1000 * 3600 * 24).toLong()

    fun getDate(formatStr: String?, date: Date): Int {
        val format = SimpleDateFormat(formatStr, Locale.getDefault())
        return format.format(date).toInt()
    }

    /**
     * 判断一个日期是否是周末，即周六日
     *
     * @param calendar calendar
     * @return 判断一个日期是否是周末，即周六日
     */
    fun isWeekend(calendar: Calendar): Boolean {
        val week = getWeekFormCalendar(calendar)
        return week == 0 || week == 6
    }

    /**
     * 获取某月的天数
     *
     * @param year  年
     * @param month 月
     * @return 某月的天数
     */
    fun getMonthDaysCount(year: Int, month: Int): Int {
        return when (month) {
            //判断大月份
            1, 3, 5, 7, 8, 10, 12 -> 31
            //判断小月
            4, 6, 9, 11 -> 30
            //判断平年与闰年
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 0
        }
    }

    /**
     * 是否是闰年
     *
     * @param year year
     * @return 是否是闰年
     */
    fun isLeapYear(year: Int): Boolean {
        return year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)
    }

    fun getMonthViewLineCount(year: Int, month: Int, weekStartWith: Int, mode: Int): Int {
        if (mode == CalendarViewDelegate.MODE_ALL_MONTH) {
            return 6
        }
        val nextDiff = getMonthEndDiff(year, month, weekStartWith)
        val preDiff = getMonthViewStartDiff(year, month, weekStartWith)
        val monthDayCount = getMonthDaysCount(year, month)
        return (preDiff + monthDayCount + nextDiff) / 7
    }

    /**
     * 获取月视图的确切高度
     * Test pass
     *
     * @param year          年
     * @param month         月
     * @param itemHeight    每项的高度
     * @param weekStartWith 周起始
     * @return 不需要多余行的高度
     */
    fun getMonthViewHeight(year: Int, month: Int, itemHeight: Int, weekStartWith: Int): Int {
        val date = java.util.Calendar.getInstance()
        date[year, month - 1, 1, 12, 0] = 0
        val preDiff = getMonthViewStartDiff(year, month, weekStartWith)
        val monthDaysCount = getMonthDaysCount(year, month)
        val nextDiff = getMonthEndDiff(year, month, monthDaysCount, weekStartWith)
        return (preDiff + monthDaysCount + nextDiff) / 7 * itemHeight
    }

    /**
     * 获取月视图的确切高度
     * Test pass
     *
     * @param year          年
     * @param month         月
     * @param itemHeight    每项的高度
     * @param weekStartWith weekStartWith
     * @param mode          mode
     * @return 不需要多余行的高度
     */
    fun getMonthViewHeight(year: Int, month: Int, itemHeight: Int, weekStartWith: Int, mode: Int): Int {
        if (mode == CalendarViewDelegate.MODE_ALL_MONTH) return itemHeight * 6
        return getMonthViewHeight(year, month, itemHeight, weekStartWith)
    }

    /**
     * 获取某天在该月的第几周,换言之就是获取这一天在该月视图的第几行,第几周，根据周起始动态获取
     * Test pass，单元测试通过
     *
     * @param calendar  calendar
     * @param weekStart 其实星期是哪一天？
     * @return 获取某天在该月的第几周 the week line in MonthView
     */
    fun getWeekFromDayInMonth(calendar: Calendar, weekStart: Int): Int {
        val date = java.util.Calendar.getInstance()
        date[calendar.year, calendar.month - 1, 1, 12, 0] = 0
        //该月第一天为星期几,星期天 == 0
        val diff = getMonthViewStartDiff(calendar, weekStart)
        return (calendar.day + diff - 1) / 7 + 1
    }

    /**
     * 获取上一个日子
     *
     * @param calendar calendar
     * @return 获取上一个日子
     */
    fun getPreCalendar(calendar: Calendar): Calendar {
        val date = java.util.Calendar.getInstance()
        date[calendar.year, calendar.month - 1, calendar.day, 12, 0] = 0
        val timeMills = date.timeInMillis //获得起始时间戳
        date.timeInMillis = timeMills - ONE_DAY

        val preCalendar = Calendar()
        preCalendar.year = date[java.util.Calendar.YEAR]
        preCalendar.month = date[java.util.Calendar.MONTH] + 1
        preCalendar.day = date[java.util.Calendar.DAY_OF_MONTH]

        return preCalendar
    }

    fun getNextCalendar(calendar: Calendar): Calendar {
        val date = java.util.Calendar.getInstance()
        date[calendar.year, calendar.month - 1, calendar.day, 12, 0] = 0
        val timeMills = date.timeInMillis //获得起始时间戳
        date.timeInMillis = timeMills + ONE_DAY

        val nextCalendar = Calendar()
        nextCalendar.year = date[java.util.Calendar.YEAR]
        nextCalendar.month = date[java.util.Calendar.MONTH] + 1
        nextCalendar.day = date[java.util.Calendar.DAY_OF_MONTH]

        return nextCalendar
    }

    /**
     * DAY_OF_WEEK return  1  2  3 	4  5  6	 7，偏移了一位
     * 获取日期所在月视图对应的起始偏移量
     * Test pass
     *
     * @param calendar  calendar
     * @param weekStart weekStart 星期的起始
     * @return 获取日期所在月视图对应的起始偏移量 the start diff with MonthView
     */
    fun getMonthViewStartDiff(calendar: Calendar, weekStart: Int): Int {
        val date = java.util.Calendar.getInstance()
        date[calendar.year, calendar.month - 1, 1, 12, 0] = 0
        val week = date[java.util.Calendar.DAY_OF_WEEK]
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) return week - 1
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) return if (week == 1) 6 else week - weekStart
        return if (week == CalendarViewDelegate.WEEK_START_WITH_SAT) 0 else week
    }

    /**
     * DAY_OF_WEEK return  1  2  3 	4  5  6	 7，偏移了一位
     * 获取日期所在月视图对应的起始偏移量
     * Test pass
     *
     * @param year      年
     * @param month     月
     * @param weekStart 周起始
     * @return 获取日期所在月视图对应的起始偏移量 the start diff with MonthView
     */
    fun getMonthViewStartDiff(year: Int, month: Int, weekStart: Int): Int {
        val date = java.util.Calendar.getInstance()
        date[year, month - 1, 1, 12, 0] = 0
        val week = date[java.util.Calendar.DAY_OF_WEEK]
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) return week - 1
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) return if (week == 1) 6 else week - weekStart
        return if (week == CalendarViewDelegate.WEEK_START_WITH_SAT) 0 else week
    }

    /**
     * DAY_OF_WEEK return  1  2  3 	4  5  6	 7，偏移了一位
     * 获取日期月份对应的结束偏移量,用于计算两个年份之间总共有多少周，不用于MonthView
     * Test pass
     *
     * @param year      年
     * @param month     月
     * @param weekStart 周起始
     * @return 获取日期月份对应的结束偏移量 the end diff in Month not MonthView
     */
    fun getMonthEndDiff(year: Int, month: Int, weekStart: Int): Int {
        return getMonthEndDiff(year, month, getMonthDaysCount(year, month), weekStart)
    }

    /**
     * DAY_OF_WEEK return  1  2  3 	4  5  6	 7，偏移了一位
     * 获取日期月份对应的结束偏移量,用于计算两个年份之间总共有多少周，不用于MonthView
     * Test pass
     *
     * @param year      年
     * @param month     月
     * @param weekStart 周起始
     * @return 获取日期月份对应的结束偏移量 the end diff in Month not MonthView
     */
    private fun getMonthEndDiff(year: Int, month: Int, day: Int, weekStart: Int): Int {
        val date = java.util.Calendar.getInstance()
        date[year, month - 1] = day
        val week = date[java.util.Calendar.DAY_OF_WEEK]
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) return 7 - week
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) return if (week == 1) 0 else 7 - week + 1
        return if (week == 7) 6 else 7 - week - 1
    }

    /**
     * 获取某个日期是星期几
     * 测试通过
     *
     * @param calendar 某个日期
     * @return 返回某个日期是星期几
     */
    fun getWeekFormCalendar(calendar: Calendar): Int {
        val date = java.util.Calendar.getInstance()
        date[calendar.year, calendar.month - 1] = calendar.day
        return date[java.util.Calendar.DAY_OF_WEEK] - 1
    }

    /**
     * 获取周视图的切换默认选项位置 WeekView index
     * 测试通过 test pass
     *
     * @param calendar  calendar
     * @param weekStart weekStart
     * @return 获取周视图的切换默认选项位置
     */
    fun getWeekViewIndexFromCalendar(calendar: Calendar, weekStart: Int): Int {
        return getWeekViewStartDiff(calendar.year, calendar.month, calendar.day, weekStart)
    }

    /**
     * 是否在日期范围內
     * 测试通过 test pass
     *
     * @param calendar     calendar
     * @param minYear      minYear
     * @param minYearDay   最小年份天
     * @param minYearMonth minYearMonth
     * @param maxYear      maxYear
     * @param maxYearMonth maxYearMonth
     * @param maxYearDay   最大年份天
     * @return 是否在日期范围內
     */
    private fun isCalendarInRange(
        calendar: Calendar,
        minYear: Int, minYearMonth: Int, minYearDay: Int,
        maxYear: Int, maxYearMonth: Int, maxYearDay: Int,
    ): Boolean {
        val cale = java.util.Calendar.getInstance()
        cale[minYear, minYearMonth - 1] = minYearDay
        val minTime = cale.timeInMillis
        cale[maxYear, maxYearMonth - 1] = maxYearDay
        val maxTime = cale.timeInMillis
        cale[calendar.year, calendar.month - 1] = calendar.day
        val curTime = cale.timeInMillis
        return curTime in minTime..maxTime
    }

    /**
     * 获取两个日期之间一共有多少周，
     * 注意周起始周一、周日、周六
     * 测试通过 test pass
     *
     * @param minYear      minYear 最小年份
     * @param minYearMonth maxYear 最小年份月份
     * @param minYearDay   最小年份天
     * @param maxYear      maxYear 最大年份
     * @param maxYearMonth maxYear 最大年份月份
     * @param maxYearDay   最大年份天
     * @param weekStart    周起始
     * @return 周数用于WeekViewPager itemCount
     */
    fun getWeekCountBetweenBothCalendar(
        minYear: Int, minYearMonth: Int, minYearDay: Int,
        maxYear: Int, maxYearMonth: Int, maxYearDay: Int,
        weekStart: Int,
    ): Int {
        val date = java.util.Calendar.getInstance()
        date[minYear, minYearMonth - 1] = minYearDay
        val minTimeMills = date.timeInMillis //给定时间戳
        val preDiff = getWeekViewStartDiff(minYear, minYearMonth, minYearDay, weekStart)
        date[maxYear, maxYearMonth - 1] = maxYearDay
        val maxTimeMills = date.timeInMillis //给定时间戳
        val nextDiff = getWeekViewEndDiff(maxYear, maxYearMonth, maxYearDay, weekStart)
        var count = preDiff + nextDiff
        val cnt = ((maxTimeMills - minTimeMills) / ONE_DAY).toInt() + 1
        count += cnt
        return count / 7
    }

    /**
     * 根据日期获取距离最小日期在第几周
     * 用来设置 WeekView currentItem
     * 测试通过 test pass
     *
     * @param calendar     calendar
     * @param minYear      minYear 最小年份
     * @param minYearMonth maxYear 最小年份月份
     * @param minYearDay   最小年份天
     * @param weekStart    周起始
     * @return 返回两个年份中第几周 the WeekView currentItem
     */
    fun getWeekFromCalendarStartWithMinCalendar(
        calendar: Calendar,
        minYear: Int, minYearMonth: Int, minYearDay: Int,
        weekStart: Int,
    ): Int {
        val date = java.util.Calendar.getInstance()
        date[minYear, minYearMonth - 1] = minYearDay //起始日期
        val firstTimeMill = date.timeInMillis //获得范围起始时间戳
        val preDiff = getWeekViewStartDiff(minYear, minYearMonth, minYearDay, weekStart) //范围起始的周偏移量
        val weekStartDiff = getWeekViewStartDiff(
            calendar.year,
            calendar.month,
            calendar.day,
            weekStart
        ) //获取点击的日子在周视图的起始，为了兼容全球时区，最大日差为一天，如果周起始偏差weekStartDiff=0，则日期加1

        date[calendar.year, calendar.month - 1] = if (weekStartDiff == 0) calendar.day + 1 else calendar.day
        val curTimeMills = date.timeInMillis //给定时间戳
        val cnt = ((curTimeMills - firstTimeMill) / ONE_DAY).toInt()
        val count = preDiff + cnt
        return count / 7 + 1
    }

    /**
     * 根据星期数和最小日期推算出该星期的第一天，
     * 为了防止夏令时，导致的时间提前和延后1-2小时，导致日期出现误差1天，因此吧hourOfDay = 12
     * //测试通过 Test pass
     *
     * @param minYear      最小年份如2017
     * @param minYearMonth maxYear 最小年份月份，like : 2017-07
     * @param minYearDay   最小年份天
     * @param week         从最小年份minYear月minYearMonth 日1 开始的第几周 week > 0
     * @param weekStart    周起始
     * @return 该星期的第一天日期
     */
    fun getFirstCalendarStartWithMinCalendar(minYear: Int, minYearMonth: Int, minYearDay: Int, week: Int, weekStart: Int): Calendar {
        val date = java.util.Calendar.getInstance()
        date[minYear, minYearMonth - 1, minYearDay, 12] = 0
        val firstTimeMills = date.timeInMillis //获得起始时间戳
        val weekTimeMills = (week - 1) * 7 * ONE_DAY
        var timeCountMills = weekTimeMills + firstTimeMills
        date.timeInMillis = timeCountMills
        val startDiff = getWeekViewStartDiff(
            date[java.util.Calendar.YEAR],
            date[java.util.Calendar.MONTH] + 1,
            date[java.util.Calendar.DAY_OF_MONTH], weekStart
        )

        timeCountMills -= startDiff * ONE_DAY
        date.timeInMillis = timeCountMills
        val calendar = Calendar()
        calendar.year = date[java.util.Calendar.YEAR]
        calendar.month = date[java.util.Calendar.MONTH] + 1
        calendar.day = date[java.util.Calendar.DAY_OF_MONTH]

        return calendar
    }

    /**
     * 是否在日期范围内
     *
     * @param calendar calendar
     * @param delegate delegate
     * @return 是否在日期范围内
     */
    fun isCalendarInRange(calendar: Calendar, delegate: CalendarViewDelegate): Boolean {
        return isCalendarInRange(
            calendar,
            delegate.minYear, delegate.minYearMonth, delegate.minYearDay,
            delegate.maxYear, delegate.maxYearMonth, delegate.maxYearDay
        )
    }

    /**
     * 是否在日期范围內
     *
     * @param year         year
     * @param month        month
     * @param minYear      minYear
     * @param minYearMonth minYearMonth
     * @param maxYear      maxYear
     * @param maxYearMonth maxYearMonth
     * @return 是否在日期范围內
     */
    fun isMonthInRange(year: Int, month: Int, minYear: Int, minYearMonth: Int, maxYear: Int, maxYearMonth: Int): Boolean {
        return !(year < minYear || year > maxYear) &&
                !(year == minYear && month < minYearMonth) &&
                !(year == maxYear && month > maxYearMonth)
    }

    /**
     * 运算 calendar1 - calendar2
     * test Pass
     *
     * @param calendar1 calendar1
     * @param calendar2 calendar2
     * @return calendar1 - calendar2
     */
    fun differ(calendar1: Calendar?, calendar2: Calendar?): Int {
        if (calendar1 == null) {
            return Int.MIN_VALUE
        }
        if (calendar2 == null) {
            return Int.MAX_VALUE
        }
        val date = java.util.Calendar.getInstance()
        date[calendar1.year, calendar1.month - 1, calendar1.day, 12, 0] = 0 //
        val startTimeMills = date.timeInMillis //获得起始时间戳
        date[calendar2.year, calendar2.month - 1, calendar2.day, 12, 0] = 0 //
        val endTimeMills = date.timeInMillis //获得结束时间戳
        return ((startTimeMills - endTimeMills) / ONE_DAY).toInt()
    }

    /**
     * 比较日期大小
     *
     * @param minYear      minYear
     * @param minYearMonth minYearMonth
     * @param minYearDay   minYearDay
     * @param maxYear      maxYear
     * @param maxYearMonth maxYearMonth
     * @param maxYearDay   maxYearDay
     * @return <0 0 >0
     */
    fun compareTo(
        minYear: Int, minYearMonth: Int, minYearDay: Int,
        maxYear: Int, maxYearMonth: Int, maxYearDay: Int,
    ): Int {
        val first = Calendar()
        first.year = minYear
        first.month = minYearMonth
        first.day = minYearDay

        val second = Calendar()
        second.year = maxYear
        second.month = maxYearMonth
        second.day = maxYearDay
        return first.compareTo(second)
    }

    /**
     * 为月视图初始化日历
     *
     * @param year        year
     * @param month       month
     * @param currentDate currentDate
     * @param weekStar    weekStar
     * @return 为月视图初始化日历项
     */
    fun initCalendarForMonthView(year: Int, month: Int, currentDate: Calendar, weekStar: Int): List<Calendar> {
        val date = java.util.Calendar.getInstance()
        date[year, month - 1] = 1
        val mPreDiff = getMonthViewStartDiff(year, month, weekStar) //获取月视图其实偏移量
        val monthDayCount = getMonthDaysCount(year, month) //获取月份真实天数

        val preYear: Int
        val preMonth: Int
        val nextYear: Int
        val nextMonth: Int

        val size = 42
        val mItems: MutableList<Calendar> = ArrayList()

        val preMonthDaysCount: Int
        when (month) {
            1 -> { //如果是1月
                preYear = year - 1
                preMonth = 12
                nextYear = year
                nextMonth = month + 1
                preMonthDaysCount = if (mPreDiff == 0) 0 else getMonthDaysCount(preYear, preMonth)
            }

            12 -> { //如果是12月
                preYear = year
                preMonth = month - 1
                nextYear = year + 1
                nextMonth = 1
                preMonthDaysCount = if (mPreDiff == 0) 0 else getMonthDaysCount(preYear, preMonth)
            }

            else -> { //平常
                preYear = year
                preMonth = month - 1
                nextYear = year
                nextMonth = month + 1
                preMonthDaysCount = if (mPreDiff == 0) 0 else getMonthDaysCount(preYear, preMonth)
            }
        }
        var nextDay = 1
        for (i in 0 until size) {
            val calendarDate = Calendar()
            when {
                i < mPreDiff -> {
                    calendarDate.year = preYear
                    calendarDate.month = preMonth
                    calendarDate.day = preMonthDaysCount - mPreDiff + i + 1
                }

                i >= monthDayCount + mPreDiff -> {
                    calendarDate.year = nextYear
                    calendarDate.month = nextMonth
                    calendarDate.day = nextDay
                    ++nextDay
                }

                else -> {
                    calendarDate.year = year
                    calendarDate.month = month
                    calendarDate.isCurrentMonth = true
                    calendarDate.day = i - mPreDiff + 1
                }
            }
            if (calendarDate == currentDate) calendarDate.isCurrentDay = true
            LunarCalendar.setupLunarCalendar(calendarDate)
            mItems.add(calendarDate)
        }
        return mItems
    }

    fun getWeekCalendars(calendar: Calendar, mDelegate: CalendarViewDelegate): List<Calendar> {
        var curTime = calendar.timeInMillis
        val date = java.util.Calendar.getInstance()
        date[calendar.year, calendar.month - 1, calendar.day, 12] = 0
        val week = date[java.util.Calendar.DAY_OF_WEEK]
        val startDiff = when (mDelegate.weekStart) {
            1 -> week - 1
            2 -> if (week == 1) 6 else week - mDelegate.weekStart
            else -> if (week == 7) 0 else week
        }

        curTime -= startDiff * ONE_DAY
        val minCalendar = java.util.Calendar.getInstance()
        minCalendar.timeInMillis = curTime
        val startCalendar = Calendar()
        startCalendar.year = minCalendar[java.util.Calendar.YEAR]
        startCalendar.month = minCalendar[java.util.Calendar.MONTH] + 1
        startCalendar.day = minCalendar[java.util.Calendar.DAY_OF_MONTH]
        return initCalendarForWeekView(startCalendar, mDelegate, mDelegate.weekStart)
    }

    /**
     * 生成周视图的7个item
     *
     * @param calendar  周视图的第一个日子calendar，所以往后推迟6天，生成周视图
     * @param mDelegate mDelegate
     * @param weekStart weekStart
     * @return 生成周视图的7个item
     */
    fun initCalendarForWeekView(calendar: Calendar, mDelegate: CalendarViewDelegate, weekStart: Int): List<Calendar> {
        val date = java.util.Calendar.getInstance() //当天时间
        date[calendar.year, calendar.month - 1, calendar.day, 12] = 0
        val curDateMills = date.timeInMillis //生成选择的日期时间戳

        //int weekEndDiff = getWeekViewEndDiff(calendar.getYear(), calendar.getMonth(), calendar.getDay(), weekStart);
        //weekEndDiff 例如周起始为周日1，当前为2020-04-01，周三，则weekEndDiff为本周结束相差今天三天，weekEndDiff=3
        val weekEndDiff = 6
        val mItems: MutableList<Calendar> = ArrayList()

        date.timeInMillis = curDateMills
        val selectCalendar = Calendar()
        selectCalendar.year = calendar.year
        selectCalendar.month = calendar.month
        selectCalendar.day = calendar.day
        if (selectCalendar == mDelegate.currentDay) selectCalendar.isCurrentDay = true
        LunarCalendar.setupLunarCalendar(selectCalendar)
        selectCalendar.isCurrentMonth = true
        mItems.add(selectCalendar)

        for (i in 1..weekEndDiff) {
            date.timeInMillis = curDateMills + i * ONE_DAY
            val calendarDate = Calendar()
            calendarDate.year = date[java.util.Calendar.YEAR]
            calendarDate.month = date[java.util.Calendar.MONTH] + 1
            calendarDate.day = date[java.util.Calendar.DAY_OF_MONTH]
            if (calendarDate == mDelegate.currentDay) calendarDate.isCurrentDay = true
            LunarCalendar.setupLunarCalendar(calendarDate)
            calendarDate.isCurrentMonth = true
            mItems.add(calendarDate)
        }
        return mItems
    }

    /**
     * 单元测试通过
     * 从选定的日期，获取周视图起始偏移量，用来生成周视图布局
     *
     * @param year      year
     * @param month     month
     * @param day       day
     * @param weekStart 周起始，1，2，7 日 一 六
     * @return 获取周视图起始偏移量，用来生成周视图布局
     */
    private fun getWeekViewStartDiff(year: Int, month: Int, day: Int, weekStart: Int): Int {
        val date = java.util.Calendar.getInstance()
        date[year, month - 1, day, 12] = 0
        val week = date[java.util.Calendar.DAY_OF_WEEK]
        if (weekStart == 1) return week - 1
        if (weekStart == 2) return if (week == 1) 6 else week - weekStart
        return if (week == 7) 0 else week
    }

    /**
     * 单元测试通过
     * 从选定的日期，获取周视图结束偏移量，用来生成周视图布局
     * 为了兼容DST，DST时区可能出现时间偏移1-2小时，从而导致凌晨时候实际获得的日期往前或者往后推移了一天，
     * 日历没有时和分的概念，因此把日期的时间强制在12:00，可以避免DST兼容问题
     *
     * @param year      year
     * @param month     month
     * @param day       day
     * @param weekStart 周起始，1，2，7 日 一 六
     * @return 获取周视图结束偏移量，用来生成周视图布局
     */
    private fun getWeekViewEndDiff(year: Int, month: Int, day: Int, weekStart: Int): Int {
        val date = java.util.Calendar.getInstance()
        date[year, month - 1, day, 12] = 0
        val week = date[java.util.Calendar.DAY_OF_WEEK]
        if (weekStart == 1) return 7 - week
        if (weekStart == 2) return if (week == 1) 0 else 7 - week + 1
        return if (week == 7) 6 else 7 - week - 1
    }

    /**
     * 从月视图切换获得第一天的日期
     * Test Pass 它是100%正确的
     *
     * @param position position
     * @param delegate position
     * @return 从月视图切换获得第一天的日期
     */
    fun getFirstCalendarFromMonthViewPager(position: Int, delegate: CalendarViewDelegate): Calendar {
        var calendar = Calendar()
        calendar.year = (position + delegate.minYearMonth - 1) / 12 + delegate.minYear
        calendar.month = (position + delegate.minYearMonth - 1) % 12 + 1
        if (delegate.defaultCalendarSelectDay != CalendarViewDelegate.FIRST_DAY_OF_MONTH) {
            val monthDays = getMonthDaysCount(calendar.year, calendar.month)
            val indexCalendar = delegate.mIndexCalendar
            calendar.day = if (indexCalendar.day == 0) 1 else if (monthDays < indexCalendar.day) monthDays else indexCalendar.day
        }
        else calendar.day = 1
        if (!isCalendarInRange(calendar, delegate)) {
            calendar = if (isMinRangeEdge(calendar, delegate)) delegate.minRangeCalendar else delegate.maxRangeCalendar
        }
        calendar.isCurrentMonth = calendar.year == delegate.currentDay.year && calendar.month == delegate.currentDay.month
        calendar.isCurrentDay = calendar == delegate.currentDay
        LunarCalendar.setupLunarCalendar(calendar)
        return calendar
    }

    /**
     * 根据传入的日期获取边界访问日期，要么最大，要么最小
     *
     * @param calendar calendar
     * @param delegate delegate
     * @return 获取边界访问日期
     */
    fun getRangeEdgeCalendar(calendar: Calendar, delegate: CalendarViewDelegate): Calendar {
        if (isCalendarInRange(delegate.currentDay, delegate) && delegate.defaultCalendarSelectDay != CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY_IGNORE_CURRENT) {
            return delegate.createCurrentDate()
        }
        if (isCalendarInRange(calendar, delegate)) return calendar
        val minRangeCalendar = delegate.minRangeCalendar
        if (minRangeCalendar.isSameMonth(calendar)) return delegate.minRangeCalendar
        return delegate.maxRangeCalendar
    }

    /**
     * 是否是最小访问边界了
     *
     * @param calendar calendar
     * @return 是否是最小访问边界了
     */
    private fun isMinRangeEdge(calendar: Calendar, delegate: CalendarViewDelegate): Boolean {
        val cale = java.util.Calendar.getInstance()
        cale[delegate.minYear, delegate.minYearMonth - 1, delegate.minYearDay, 12] = 0
        val minTime = cale.timeInMillis
        cale[calendar.year, calendar.month - 1, calendar.day, 12] = 0
        val curTime = cale.timeInMillis
        return curTime < minTime
    }

    /**
     * dp转px
     *
     * @param context context
     * @param dp dp
     * @return px
     */
    fun dipToPx(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}
