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
import android.text.TextUtils
import com.cygnet.caleviewdemo.R
import com.cygnet.caleviewdemo.calendar.CalendarUtil.getWeekFormCalendar
import com.cygnet.caleviewdemo.calendar.CalendarUtil.isLeapYear
import com.cygnet.caleviewdemo.calendar.CalendarUtil.isWeekend

/**
 * 农历计算相关
 */
object LunarCalendar {

    fun init(context: Context) {
        if (MONTH_STR != null) return
        TrunkBranchAnnals.init(context)
        SolarTermUtil.init(context)
        MONTH_STR = context.resources.getStringArray(R.array.lunar_first_of_month)
        TRADITION_FESTIVAL_STR = context.resources.getStringArray(R.array.tradition_festival)
        DAY_STR = context.resources.getStringArray(R.array.lunar_str)
        SPECIAL_FESTIVAL_STR = context.resources.getStringArray(R.array.special_festivals)
        SOLAR_CALENDAR = context.resources.getStringArray(R.array.solar_festival)
    }

    /**
     * 农历月份第一天转写
     */
    private var MONTH_STR: Array<String>? = null

    /**
     * 传统农历节日
     */
    private var TRADITION_FESTIVAL_STR: Array<String>? = null

    /**
     * 农历大写
     */
    private var DAY_STR: Array<String>? = null

    /**
     * 特殊节日的数组
     */
    private var SPECIAL_FESTIVAL_STR: Array<String>? = null

    /**
     * 特殊节日、母亲节和父亲节,感恩节等
     */
    private val SPECIAL_FESTIVAL: MutableMap<Int, Array<String?>> = HashMap()

    /**
     * 公历节日
     */
    private var SOLAR_CALENDAR: Array<String>? = null

    /**
     * 保存每年24节气
     */
    private val SOLAR_TERMS: MutableMap<Int, Array<String>> = HashMap()

    /**
     * 返回传统农历节日
     *
     * @param year  农历年
     * @param month 农历月
     * @param day   农历日
     * @return 返回传统农历节日
     */
    private fun getTraditionFestival(year: Int, month: Int, day: Int): String {
        if (month == 12) {
            val count = daysInLunarMonth(year, month)
            if (day == count) return TRADITION_FESTIVAL_STR!![0] //除夕
        }
        val text = getString(month, day)
        var festivalStr = ""
        for (festival in TRADITION_FESTIVAL_STR!!) {
            if (festival.contains(text)) {
                festivalStr = festival.replace(text, "")
                break
            }
        }
        return festivalStr
    }

    /**
     * 数字转换为汉字月份
     *
     * @param month 月
     * @param leap  1==闰月
     * @return 数字转换为汉字月份
     */
    private fun numToChineseMonth(month: Int, leap: Int): String {
        if (leap == 1) return "闰" + MONTH_STR!![month - 1]
        return MONTH_STR!![month - 1]
    }

    /**
     * 数字转换为农历节日或者日期
     *
     * @param month 月
     * @param day   日
     * @param leap  1==闰月
     * @return 数字转换为汉字日
     */
    private fun numToChinese(month: Int, day: Int, leap: Int): String {
        if (day == 1) return numToChineseMonth(month, leap)
        return DAY_STR!![day - 1]
    }

    /**
     * 用来表示1900年到2099年间农历年份的相关信息，共24位bit的16进制表示，其中：
     * 1. 前4位表示该年闰哪个月；
     * 2. 5-17位表示农历年份13个月的大小月分布，0表示小，1表示大；
     * 3. 最后7位表示农历年首（正月初一）对应的公历日期。
     *
     *
     * 以2014年的数据0x955ABF为例说明：
     * 1001 0101 0101 1010 1011 1111
     * 闰九月 农历正月初一对应公历1月31号
     */
    private val LUNAR_INFO = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,  //1900-1909
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,  //1910-1919
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,  //1920-1929
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,  //1930-1939
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,  //1940-1949
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,  //1950-1959
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,  //1960-1969
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,  //1970-1979
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,  //1980-1989
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,  //1990-1999
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,  //2000-2009
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,  //2010-2019
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,  //2020-2029
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,  //2030-2039
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,  //2040-2049
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,  //2050-2059
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,  //2060-2069
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,  //2070-2079
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,  //2080-2089
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,  //2090-2099
        0x0d520
    )

    /**
     * 农历 year年month月的总天数，总共有13个月包括闰月
     *
     * @param year  将要计算的年份
     * @param month 将要计算的月份
     * @return 传回农历 year年month月的总天数
     */
    private fun daysInLunarMonth(year: Int, month: Int): Int {
        return if ((LUNAR_INFO[year - CalendarViewDelegate.MIN_YEAR] and (0x10000 shr month)) == 0) 29 else 30
    }

    /**
     * 获取公历节日
     *
     * @param month 公历月份
     * @param day   公历日期
     * @return 公历节日
     */
    private fun gregorianFestival(month: Int, day: Int): String {
        val text = getString(month, day)
        var solar = ""
        for (cale in SOLAR_CALENDAR!!) {
            if (cale.contains(text)) {
                solar = cale.replace(text, "")
                break
            }
        }
        return solar
    }

    private fun getString(month: Int, day: Int): String {
        return (if (month >= 10) month.toString() else "0$month") + (if (day >= 10) day else "0$day")
    }

    /**
     * 返回24节气
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 返回24节气
     */
    private fun getSolarTerm(year: Int, month: Int, day: Int): String {
        if (!SOLAR_TERMS.containsKey(year)) SOLAR_TERMS[year] = SolarTermUtil.getSolarTerms(year)
        val solarTerm = SOLAR_TERMS[year]
        val text = year.toString() + getString(month, day)
        var solar = ""
        assert(solarTerm != null)
        for (solarName in solarTerm!!) {
            if (solarName.contains(text)) {
                solar = solarName.replace(text, "")
                break
            }
        }
        return solar
    }

    /**
     * 获取农历节日
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return 农历节日
     */
    private fun getLunarText(year: Int, month: Int, day: Int): String {
        val termText = getSolarTerm(year, month, day)
        val solar = gregorianFestival(month, day)
        if (!TextUtils.isEmpty(solar)) return solar
        if (!TextUtils.isEmpty(termText)) return termText
        val lunar = LunarUtil.solarToLunar(year, month, day)
        val festival = getTraditionFestival(lunar[0], lunar[1], lunar[2])
        if (!TextUtils.isEmpty(festival)) return festival
        return numToChinese(lunar[1], lunar[2], lunar[3])
    }

    /**
     * 获取特殊计算方式的节日
     * 如：每年五月的第二个星期日为母亲节，六月的第三个星期日为父亲节
     * 每年11月第四个星期四定为"感恩节"
     *
     * @param year  year
     * @param month month
     * @param day   day
     * @return 获取西方节日
     */
    private fun getSpecialFestival(year: Int, month: Int, day: Int): String {
        if (!SPECIAL_FESTIVAL.containsKey(year)) SPECIAL_FESTIVAL[year] = getSpecialFestivals(year)
        val specialFestivals = SPECIAL_FESTIVAL[year]
        val text = year.toString() + getString(month, day)
        var solar = ""
        assert(specialFestivals != null)
        for (special in specialFestivals!!) {
            if (special!!.contains(text)) {
                solar = special.replace(text, "")
                break
            }
        }
        return solar
    }

    /**
     * 获取每年的母亲节和父亲节和感恩节
     * 特殊计算方式的节日
     *
     * @param year 年
     * @return 获取每年的母亲节和父亲节、感恩节
     */
    private fun getSpecialFestivals(year: Int): Array<String?> {
        val festivals = arrayOfNulls<String>(3)
        val date = java.util.Calendar.getInstance()
        date[year, 4] = 1
        var week = date[java.util.Calendar.DAY_OF_WEEK]
        var startDiff = 7 - week + 1
        if (startDiff == 7) festivals[0] = dateToString(year, 5, startDiff + 1) + SPECIAL_FESTIVAL_STR!![0]
        else festivals[0] = dateToString(year, 5, startDiff + 7 + 1) + SPECIAL_FESTIVAL_STR!![0]
        date[year, 5] = 1
        week = date[java.util.Calendar.DAY_OF_WEEK]
        startDiff = 7 - week + 1
        if (startDiff == 7) festivals[1] = dateToString(year, 6, startDiff + 7 + 1) + SPECIAL_FESTIVAL_STR!![1]
        else festivals[1] = dateToString(year, 6, startDiff + 7 + 7 + 1) + SPECIAL_FESTIVAL_STR!![1]

        date[year, 10] = 1
        week = date[java.util.Calendar.DAY_OF_WEEK]
        startDiff = 7 - week + 1
        if (startDiff <= 2) festivals[2] = dateToString(year, 11, startDiff + 21 + 5) + SPECIAL_FESTIVAL_STR!![2]
        else festivals[2] = dateToString(year, 11, startDiff + 14 + 5) + SPECIAL_FESTIVAL_STR!![2]
        return festivals
    }

    private fun dateToString(year: Int, month: Int, day: Int): String {
        return year.toString() + getString(month, day)
    }

    /**
     * 初始化各种农历、节日
     *
     * @param calendar calendar
     */
    fun setupLunarCalendar(calendar: Calendar) {
        val year = calendar.year
        val month = calendar.month
        val day = calendar.day
        calendar.isWeekend = isWeekend(calendar)
        calendar.week = getWeekFormCalendar(calendar)

        val lunarCalendar = Calendar()
        calendar.lunarCalendar = lunarCalendar
        val lunar = LunarUtil.solarToLunar(year, month, day)
        lunarCalendar.year = lunar[0]
        lunarCalendar.month = lunar[1]
        lunarCalendar.day = lunar[2]
        calendar.isLeapYear = isLeapYear(year)
        if (lunar[3] == 1) { //如果是闰月
            calendar.leapMonth = lunar[1]
            lunarCalendar.leapMonth = lunar[1]
        }
        val solarTerm = getSolarTerm(year, month, day)
        var gregorian = gregorianFestival(month, day)
        val festival = getTraditionFestival(lunar[0], lunar[1], lunar[2])
        val lunarText = numToChinese(lunar[1], lunar[2], lunar[3])
        if (TextUtils.isEmpty(gregorian)) gregorian = getSpecialFestival(year, month, day)
        calendar.solarTerm = solarTerm
        calendar.gregorianFestival = gregorian
        calendar.traditionFestival = festival
        lunarCalendar.traditionFestival = festival
        lunarCalendar.solarTerm = solarTerm
        when {
            !TextUtils.isEmpty(solarTerm) -> calendar.lunar = solarTerm
            !TextUtils.isEmpty(gregorian) -> calendar.lunar = gregorian
            !TextUtils.isEmpty(festival) -> calendar.lunar = festival
            else -> calendar.lunar = lunarText
        }
        lunarCalendar.lunar = lunarText
    }

    /**
     * 获取农历节日
     *
     * @param calendar calendar
     * @return 获取农历节日
     */
    fun getLunarText(calendar: Calendar): String {
        return getLunarText(calendar.year, calendar.month, calendar.day)
    }
}
