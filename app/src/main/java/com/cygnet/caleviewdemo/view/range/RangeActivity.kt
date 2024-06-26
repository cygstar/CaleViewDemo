package com.cygnet.caleviewdemo.view.range

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.cygnet.caleviewdemo.R
import com.cygnet.caleviewdemo.adapter.base.BaseActivity
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.CalendarView
import com.cygnet.caleviewdemo.calendar.CalendarView.OnCalendarInterceptListener
import com.cygnet.caleviewdemo.calendar.CalendarView.OnCalendarRangeSelectListener
import com.cygnet.caleviewdemo.calendar.CalendarView.OnMonthChangeListener

class RangeActivity : BaseActivity(), OnCalendarInterceptListener, OnCalendarRangeSelectListener, OnMonthChangeListener, View.OnClickListener {

    private lateinit var mTextLeftDate: TextView
    private lateinit var mTextLeftWeek: TextView
    private lateinit var mTextRightDate: TextView
    private lateinit var mTextRightWeek: TextView
    private lateinit var mTextMinRange: TextView
    private lateinit var mTextMaxRange: TextView
    private lateinit var mCalendarView: CalendarView
    private var mCalendarHeight = 0

    override val layoutId: Int
        get() = R.layout.activity_range

    override fun initView() {
        setStatusBarDarkMode()
        mTextLeftDate = findViewById(R.id.tv_left_date)
        mTextLeftWeek = findViewById(R.id.tv_left_week)
        mTextRightDate = findViewById(R.id.tv_right_date)
        mTextRightWeek = findViewById(R.id.tv_right_week)

        mTextMinRange = findViewById(R.id.tv_min_range)
        mTextMaxRange = findViewById(R.id.tv_max_range)

        mCalendarView = findViewById(R.id.calendarView)
        mCalendarView.setOnCalendarRangeSelectListener(this)
        mCalendarView.setOnMonthChangeListener(this)
        //设置日期拦截事件，当前有效
        mCalendarView.setOnCalendarInterceptListener(this)

        findViewById<View>(R.id.iv_clear).setOnClickListener(this)
        findViewById<View>(R.id.iv_reduce).setOnClickListener(this)
        findViewById<View>(R.id.iv_increase).setOnClickListener(this)
        findViewById<View>(R.id.tv_commit).setOnClickListener(this)
        findViewById<View>(R.id.tv_title).setOnClickListener(this)

        mCalendarHeight = dipToPx(this, 46f)

        mCalendarView.setRange(
            2000, 1, 1,
            mCalendarView.curYear, mCalendarView.curMonth, mCalendarView.curDay
        )
        mCalendarView.post { mCalendarView.scrollToCurrent() }
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
        map[getSchemeCalendar(year, month, 25, -0xec5310, "假").toString()] = getSchemeCalendar(year, month, 25, -0xec5310, "假")
        map[getSchemeCalendar(year, month, 27, -0xec5310, "多").toString()] = getSchemeCalendar(year, month, 27, -0xec5310, "多")
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        mCalendarView.setSchemeDate(map)

        mTextMinRange.text = String.format("min range = %s", mCalendarView.minSelectRange)
        mTextMaxRange.text = String.format("max range = %s", mCalendarView.maxSelectRange)
    }

    private fun getSchemeCalendar(year: Int, month: Int, day: Int, color: Int, text: String): Calendar {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = color //如果单独标记颜色、则会使用这个颜色
        calendar.scheme = text
        calendar.addScheme(Calendar.Scheme())
        calendar.addScheme(-0xff7800, "假")
        calendar.addScheme(-0xff7800, "节")
        return calendar
    }

    override fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.tv_title -> {}
            R.id.iv_clear -> {
                mCalendarView.clearSelectRange()
                mTextLeftWeek.text = "开始日期"
                mTextRightWeek.text = "结束日期"
                mTextLeftDate.text = ""
                mTextRightDate.text = ""
                //mCalendarView.setSelectCalendarRange(2018,10,13,2018,10,13);
            }

            R.id.iv_reduce -> {
                mCalendarHeight -= dipToPx(this, 8f)
                if (mCalendarHeight <= dipToPx(this, 46f)) {
                    mCalendarHeight = dipToPx(this, 46f)
                }
                mCalendarView.setCalendarItemHeight(mCalendarHeight)
            }

            R.id.iv_increase -> {
                mCalendarHeight += dipToPx(this, 8f)
                if (mCalendarHeight >= dipToPx(this, 90f)) {
                    mCalendarHeight = dipToPx(this, 90f)
                }
                mCalendarView.setCalendarItemHeight(mCalendarHeight)
            }

            R.id.tv_commit -> {
                val calendars = mCalendarView.selectCalendarRange
                if (calendars == null || calendars.isEmpty()) {
                    return
                }
                for (cale in calendars) {
                    Log.e(
                        "SelectCalendarRange", cale.toString()
                                + " -- " + cale.scheme
                                + "  --  " + cale.lunar
                    )
                }
                Toast.makeText(
                    this, String.format(
                        "选择了%s个日期: %s —— %s", calendars.size,
                        calendars[0].toString(), calendars[calendars.size - 1].toString()
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * 屏蔽某些不可点击的日期，可根据自己的业务自行修改
     * 如 calendar > 2018年1月1日 && calendar <= 2020年12月31日
     *
     * @param calendar calendar
     * @return 是否屏蔽某些不可点击的日期，MonthView和WeekView有类似的API可调用
     */
    override fun onCalendarIntercept(calendar: Calendar): Boolean {
        return false
        //return calendar.getTimeInMillis()<getCurrentDayMill() ;
    }

    private val currentDayMill: Long
        get() {
            val calendar = java.util.Calendar.getInstance()
            calendar[java.util.Calendar.HOUR] = 0
            calendar[java.util.Calendar.MINUTE] = 0
            calendar[java.util.Calendar.MILLISECOND] = 0
            return calendar.timeInMillis
        }

    override fun onCalendarInterceptClick(calendar: Calendar, isClick: Boolean) {
        Toast.makeText(
            this,
            calendar.toString() + (if (isClick) "拦截不可点击" else "拦截设定为无效日期"),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onMonthChange(year: Int, month: Int) {
        Log.e("onMonthChange", "  -- $year  --  $month")
    }

    // 超出范围提示
    override fun onCalendarSelectOutOfRange(calendar: Calendar) {}

    override fun onSelectOutOfRange(calendar: Calendar, isOutOfMinRange: Boolean) {
        Toast.makeText(
            this,
            calendar.toString() + (if (isOutOfMinRange) "小于最小选择范围" else "超过最大选择范围"),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCalendarRangeSelect(calendar: Calendar, isEnd: Boolean) {
        if (!isEnd) {
            mTextLeftDate.text = String.format(calendar.month.toString() + "月" + calendar.day + "日")
            mTextLeftWeek.text = WEEK[calendar.week]
            mTextRightWeek.text = "结束日期"
            mTextRightDate.text = ""
        }
        else {
            mTextRightDate.text = String.format(calendar.month.toString() + "月" + calendar.day + "日")
            mTextRightWeek.text = WEEK[calendar.week]
        }
    }

    companion object {
        fun show(context: Context) {
            context.startActivity(Intent(context, RangeActivity::class.java))
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

        private val WEEK = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
    }
}
