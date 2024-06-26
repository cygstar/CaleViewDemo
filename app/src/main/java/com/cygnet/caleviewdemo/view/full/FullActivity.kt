package com.cygnet.caleviewdemo.view.full

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import com.cygnet.caleviewdemo.R
import com.cygnet.caleviewdemo.adapter.base.BaseActivity
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.CalendarView
import com.cygnet.caleviewdemo.calendar.CalendarView.OnCalendarSelectListener
import com.cygnet.caleviewdemo.calendar.CalendarView.OnYearChangeListener

/**
 * 全屏日历,因为月份大小不同的关系，需要 app:month_view_show_mode="mode_all"，
 * 一般地，既然使用全屏日历，那也不必要
 * Created by huanghaibin on 2019/3/7.
 */
class FullActivity : BaseActivity(), OnCalendarSelectListener, OnYearChangeListener, View.OnClickListener {

    lateinit var mTextMonthDay: TextView
    lateinit var mTextYear: TextView
    lateinit var mTextLunar: TextView
    lateinit var mTextCurrentDay: TextView
    lateinit var mCalendarView: CalendarView

    private var mYear = 0

    override val layoutId: Int
        get() = R.layout.activity_full

    override fun initView() {
        setStatusBarDarkMode()
        mTextMonthDay = findViewById(R.id.tv_month_day)
        mTextYear = findViewById(R.id.tv_year)
        mTextLunar = findViewById(R.id.tv_lunar)
        mTextCurrentDay = findViewById(R.id.tv_current_day)
        mCalendarView = findViewById(R.id.calendarView)
        mTextMonthDay.setOnClickListener {
            mCalendarView.showYearSelectLayout(mYear)
            mTextLunar.visibility = View.GONE
            mTextYear.visibility = View.GONE
            mTextMonthDay.text = mYear.toString()
        }
        findViewById<View>(R.id.fl_current).setOnClickListener { mCalendarView.scrollToCurrent() }

        mCalendarView.setOnCalendarSelectListener(this)
        mCalendarView.setOnYearChangeListener(this)
        mTextYear.text = mCalendarView.curYear.toString()
        mYear = mCalendarView.curYear
        mTextMonthDay.text = String.format(mCalendarView.curMonth.toString() + "月" + mCalendarView.curDay + "日")
        mTextLunar.text = "今日"
        mTextCurrentDay.text = mCalendarView.curDay.toString()
    }

    override fun initData() {
        val year = mCalendarView.curYear
        val month = mCalendarView.curMonth

        val map: MutableMap<String, Calendar> = HashMap()
        map[getSchemeCalendar(year, month, 3, -0xbf24db, "假").toString()] = getSchemeCalendar(year, month, 3, -0xbf24db, "假")
        map[getSchemeCalendar(year, month, 6, -0x196ec8, "事").toString()] = getSchemeCalendar(year, month, 6, -0x196ec8, "事")
        map[getSchemeCalendar(year, month, 9, -0x20ecaa, "议").toString()] = getSchemeCalendar(year, month, 9, -0x20ecaa, "议")
        map[getSchemeCalendar(year, month, 13, -0x123a93, "记").toString()] = getSchemeCalendar(year, month, 13, -0x123a93, "记")
        map[getSchemeCalendar(year, month, 14, -0x123a93, "记").toString()] = getSchemeCalendar(year, month, 14, -0x123a93, "记")
        map[getSchemeCalendar(year, month, 15, -0x5533bc, "假").toString()] = getSchemeCalendar(year, month, 15, -0x5533bc, "假")
        map[getSchemeCalendar(year, month, 18, -0x43ec10, "记").toString()] = getSchemeCalendar(year, month, 18, -0x43ec10, "记")
        map[getSchemeCalendar(year, month, 22, -0x20ecaa, "议").toString()] = getSchemeCalendar(year, month, 22, -0x20ecaa, "议")
        map[getSchemeCalendar(year, month, 25, -0xec5310, "假").toString()] = getSchemeCalendar(year, month, 25, -0xec5310, "假")
        map[getSchemeCalendar(year, month, 27, -0xec5310, "多").toString()] = getSchemeCalendar(year, month, 27, -0xec5310, "多")
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        mCalendarView.setSchemeDate(map)
    }

    private fun getSchemeCalendar(year: Int, month: Int, day: Int, color: Int, text: String): Calendar {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = color //如果单独标记颜色、则会使用这个颜色
        calendar.scheme = text
        calendar.addScheme(color, "假")
        calendar.addScheme(if (day % 2 == 0) -0xff3300 else -0x2ea012, "节")
        calendar.addScheme(if (day % 2 == 0) -0x9a0000 else -0xbe961f, "记")
        return calendar
    }

    override fun onClick(v: View) {
        val id = v.id
    }

    override fun onCalendarOutOfRange(calendar: Calendar) {}

    override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {
        mTextLunar.visibility = View.VISIBLE
        mTextYear.visibility = View.VISIBLE
        mTextMonthDay.text = String.format(calendar.month.toString() + "月" + calendar.day + "日")
        mTextYear.text = calendar.year.toString()
        mTextLunar.text = calendar.lunar
        mYear = calendar.year

        Log.e(
            "onDateSelected", "  -- " + calendar.year +
                    "  --  " + calendar.month +
                    "  -- " + calendar.day +
                    "  --  " + isClick + "  --   " + calendar.scheme
        )
    }

    override fun onYearChange(year: Int) {
        mTextMonthDay.text = year.toString()
    }

    companion object {
        fun show(context: Context) {
            context.startActivity(Intent(context, FullActivity::class.java))
        }
    }
}
