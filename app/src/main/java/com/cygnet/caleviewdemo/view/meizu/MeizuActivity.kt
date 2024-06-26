package com.cygnet.caleviewdemo.view.meizu

import android.content.Context
import android.content.Intent
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

class MeizuActivity : BaseActivity(), OnCalendarSelectListener, OnYearChangeListener, View.OnClickListener {

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
        get() = R.layout.activity_meizu

    override fun initView() {
        setStatusBarDarkMode()
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

        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView.setLayoutManager(LinearLayoutManager(this))

        mRecyclerView.addItemDecoration(GroupItemDecoration<String, Article>())
        mRecyclerView.adapter = ArticleAdapter(this)
        mRecyclerView.notifyDataSetChanged()

    }

    override fun onClick(v: View) {
        val id = v.id
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
            context.startActivity(Intent(context, MeizuActivity::class.java))
        }
    }
}
