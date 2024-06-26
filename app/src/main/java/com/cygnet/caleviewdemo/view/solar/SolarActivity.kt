package com.cygnet.caleviewdemo.view.solar

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.cygnet.caleviewdemo.R
import com.cygnet.caleviewdemo.adapter.Article
import com.cygnet.caleviewdemo.adapter.ArticleAdapter
import com.cygnet.caleviewdemo.adapter.base.BaseActivity
import com.cygnet.caleviewdemo.adapter.group.GroupItemDecoration
import com.cygnet.caleviewdemo.adapter.group.GroupRecyclerView
import com.cygnet.caleviewdemo.calendar.Calendar
import com.cygnet.caleviewdemo.calendar.CalendarLayout
import com.cygnet.caleviewdemo.calendar.CalendarView
import com.cygnet.caleviewdemo.calendar.CalendarView.OnCalendarSelectListener
import com.cygnet.caleviewdemo.calendar.CalendarView.OnYearChangeListener

class SolarActivity : BaseActivity(), OnCalendarSelectListener, OnYearChangeListener, View.OnClickListener {

    lateinit var mTextMonthDay: TextView
    lateinit var mTextYear: TextView
    lateinit var mTextLunar: TextView
    lateinit var mTextCurrentDay: TextView
    lateinit var mCalendarView: CalendarView
    lateinit var mRelativeTool: RelativeLayout
    lateinit var mCalendarLayout: CalendarLayout
    lateinit var mRecyclerView: GroupRecyclerView

    private var mYear = 0

    override val layoutId: Int
        get() = R.layout.activity_solar

    override fun initView() {
        if (Build.VERSION.SDK_INT >= 21) window.statusBarColor = resources.getColor(R.color.solar_background, null)
        mTextMonthDay = findViewById(R.id.tv_month_day)
        mTextYear = findViewById(R.id.tv_year)
        mTextLunar = findViewById(R.id.tv_lunar)
        mRelativeTool = findViewById(R.id.rl_tool)
        mCalendarView = findViewById(R.id.calendarView)
        mTextCurrentDay = findViewById(R.id.tv_current_day)
        mTextMonthDay.setOnClickListener(View.OnClickListener {
            if (!mCalendarLayout.isExpand) {
                mCalendarLayout.expand()
                return@OnClickListener
            }
            mCalendarView.showYearSelectLayout(mYear)
            mTextLunar.visibility = View.GONE
            mTextYear.visibility = View.GONE
            mTextMonthDay.text = mYear.toString()
        })
        findViewById<View>(R.id.fl_current).setOnClickListener { mCalendarView.scrollToCurrent() }
        mCalendarLayout = findViewById(R.id.calendarLayout)
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
        map[getSchemeCalendar(year, month, 3, "假").toString()] = getSchemeCalendar(year, month, 3, "假")
        map[getSchemeCalendar(year, month, 6, "事").toString()] = getSchemeCalendar(year, month, 6, "事")
        map[getSchemeCalendar(year, month, 9, "议").toString()] = getSchemeCalendar(year, month, 9, "议")
        map[getSchemeCalendar(year, month, 13, "记").toString()] = getSchemeCalendar(year, month, 13, "记")
        map[getSchemeCalendar(year, month, 14, "记").toString()] = getSchemeCalendar(year, month, 14, "记")
        map[getSchemeCalendar(year, month, 15, "假").toString()] = getSchemeCalendar(year, month, 15, "假")
        map[getSchemeCalendar(year, month, 18, "记").toString()] = getSchemeCalendar(year, month, 18, "记")
        map[getSchemeCalendar(year, month, 25, "假").toString()] = getSchemeCalendar(year, month, 25, "假")
        map[getSchemeCalendar(year, month, 27, "多").toString()] = getSchemeCalendar(year, month, 27, "多")
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        mCalendarView.setSchemeDate(map)

        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView.setLayoutManager(LinearLayoutManager(this))

        mRecyclerView.addItemDecoration(GroupItemDecoration<String, Article>())
        mRecyclerView.adapter = ArticleAdapter(this)
        mRecyclerView.notifyDataSetChanged()

    }

    override fun onClick(v: View) {
        val id = v.id
    }

    private fun getSchemeCalendar(year: Int, month: Int, day: Int, text: String): Calendar {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = Color.WHITE
        calendar.scheme = text
        calendar.addScheme(-0x574feb, "rightTop")
        calendar.addScheme(-0xbdc350, "leftTop")
        calendar.addScheme(-0x9bc374, "bottom")

        return calendar
    }

    override fun onCalendarOutOfRange(calendar: Calendar) {}

    override fun onCalendarSelect(calendar: Calendar, isClick: Boolean) {
        mTextLunar.visibility = View.VISIBLE
        mTextYear.visibility = View.VISIBLE
        mTextMonthDay.text = String.format(calendar.month.toString() + "月" + calendar.day + "日")
        mTextYear.text = calendar.year.toString()
        mTextLunar.text = calendar.lunar
        mYear = calendar.year
    }

    override fun onYearChange(year: Int) {
        mTextMonthDay.text = year.toString()
    }

    companion object {
        fun show(context: Context) {
            context.startActivity(Intent(context, SolarActivity::class.java))
        }
    }
}
