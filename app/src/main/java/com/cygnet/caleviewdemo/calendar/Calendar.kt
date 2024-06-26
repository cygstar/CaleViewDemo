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

import android.text.TextUtils
import java.io.Serializable

/**
 * 日历对象、
 */
class Calendar : Serializable, Comparable<Calendar> {
    /**
     * 年
     */
    var year: Int = 0

    /**
     * 月1-12
     */
    var month: Int = 0

    /**
     * 如果是闰月，则返回闰月
     */
    var leapMonth: Int = 0

    /**
     * 日1-31
     */
    var day: Int = 0

    /**
     * 是否是闰年
     */
    var isLeapYear: Boolean = false

    /**
     * 是否是本月,这里对应的是月视图的本月，而非当前月份，请注意
     */
    var isCurrentMonth: Boolean = false

    /**
     * 是否是今天
     */
    var isCurrentDay: Boolean = false

    /**
     * 农历字符串，没有特别大的意义，用来做简单的农历或者节日标记
     * 建议通过lunarCalendar获取完整的农历日期
     */
    var lunar: String? = null

    /**
     * 24节气
     */
    var solarTerm: String? = null

    /**
     * 公历节日
     */
    var gregorianFestival: String? = null

    /**
     * 传统农历节日
     */
    var traditionFestival: String? = null

    /**
     * 计划，可以用来标记当天是否有任务,这里是默认的，如果使用多标记，请使用下面API
     * using addScheme(int schemeColor,String scheme); multi scheme
     */
    var scheme: String? = null

    /**
     * 各种自定义标记颜色、没有则选择默认颜色，如果使用多标记，请使用下面API
     * using addScheme(int schemeColor,String scheme); multi scheme
     */
    var schemeColor: Int = 0

    /**
     * 多标记
     * multi scheme,using addScheme();
     */
    var schemes: MutableList<Scheme>? = null

    /**
     * 是否是周末
     */
    var isWeekend: Boolean = false

    /**
     * 星期,0-6 对应周日到周一
     */
    var week: Int = 0

    /**
     * 获取完整的农历日期
     */
    var lunarCalendar: Calendar? = null

    fun addScheme(scheme: Scheme) {
        if (schemes == null) schemes = ArrayList()
        schemes?.add(scheme)
    }

    fun addScheme(schemeColor: Int, scheme: String) {
        if (schemes == null) schemes = ArrayList()
        schemes?.add(Scheme(schemeColor, scheme))
    }

    fun addScheme(type: Int, schemeColor: Int, scheme: String) {
        if (schemes == null) schemes = ArrayList()
        schemes?.add(Scheme(type, schemeColor, scheme))
    }

    fun addScheme(type: Int, schemeColor: Int, scheme: String, other: String) {
        if (schemes == null) schemes = ArrayList()
        schemes?.add(Scheme(type, schemeColor, scheme, other))
    }

    fun addScheme(schemeColor: Int, scheme: String, other: String) {
        if (schemes == null) schemes = ArrayList()
        schemes?.add(Scheme(schemeColor, scheme, other))
    }

    fun hasScheme(): Boolean {
        if (schemes != null && schemes?.size != 0) return true
        if (!TextUtils.isEmpty(scheme)) return true
        return false
    }

    /**
     * 是否是相同月份
     *
     * @param calendar 日期
     * @return 是否是相同月份
     */
    fun isSameMonth(calendar: Calendar): Boolean {
        return year == calendar.year && month == calendar.month
    }

    /**
     * 比较日期
     *
     * @param calendar 日期
     * @return <0 0 >0
     */
    override fun compareTo(other: Calendar): Int {
        return toString().compareTo(other.toString())
    }

    /**
     * 运算差距多少天
     *
     * @param calendar calendar
     * @return 运算差距多少天
     */
    fun differ(calendar: Calendar): Int {
        return CalendarUtil.differ(this, calendar)
    }

    val isAvailable: Boolean
        /**
         * 日期是否可用
         *
         * @return 日期是否可用
         */
        get() = (year > 0) and (month > 0) and (day > 0) and (day <= 31) and (month <= 12) and (year >= 1900) and (year <= 2099)

    val timeInMillis: Long
        /**
         * 获取当前日历对应时间戳
         *
         * @return getTimeInMillis
         */
        get() {
            val calendar = java.util.Calendar.getInstance()
            calendar[java.util.Calendar.YEAR] = year
            calendar[java.util.Calendar.MONTH] = month - 1
            calendar[java.util.Calendar.DAY_OF_MONTH] = day
            return calendar.timeInMillis
        }

    override fun equals(other: Any?): Boolean {
        if (other != null && other is Calendar) {
            if (other.year == year && (other.month == month) && (other.day == day)) return true
        }
        return super.equals(other)
    }

    override fun toString(): String {
        return year.toString() + "" + (if (month < 10) "0$month" else month) + "" + (if (day < 10) "0$day" else day)
    }

    //    @Override
    //    public int compare(Calendar lhs, Calendar rhs) {
    //        if (lhs == null || rhs == null) {
    //            return 0;
    //        }
    //        int result = lhs.compareTo(rhs);
    //        return result;
    //    }
    fun mergeScheme(calendar: Calendar?, defaultScheme: String?) {
        if (calendar == null) return
        scheme = if (TextUtils.isEmpty(calendar.scheme)) defaultScheme else calendar.scheme
        schemeColor = calendar.schemeColor
        schemes = calendar.schemes
    }

    fun clearScheme() {
        scheme = ""
        schemeColor = 0
        schemes = null
    }

    /**
     * 事件标记服务，现在多类型的事务标记建议使用这个
     */
    class Scheme : Serializable {
        private lateinit var other: String
        lateinit var scheme: String
        private var type: Int = 0
        var schemeColor: Int = 0
        var obj: Any? = null

        constructor()

        constructor(type: Int, schemeColor: Int, scheme: String, other: String) {
            this.type = type
            this.schemeColor = schemeColor
            this.scheme = scheme
            this.other = other
        }

        constructor(type: Int, schemeColor: Int, scheme: String) {
            this.type = type
            this.schemeColor = schemeColor
            this.scheme = scheme
        }

        constructor(schemeColor: Int, scheme: String) {
            this.schemeColor = schemeColor
            this.scheme = scheme
        }

        constructor(schemeColor: Int, scheme: String, other: String) {
            this.schemeColor = schemeColor
            this.scheme = scheme
            this.other = other
        }
    }
}
