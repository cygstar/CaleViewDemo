package com.cygnet.caleviewdemo.view.pager

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.cygnet.caleviewdemo.adapter.base.FragmentAdapter
import com.cygnet.caleviewdemo.calendar.CalendarLayout.CalendarScrollView

/**
 * 如果嵌套各种View出现事件冲突，可以实现这个方法即可
 */
class CalendarLinearLayout : LinearLayout, CalendarScrollView {

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    private var mAdapter: FragmentAdapter? = null

    /**
     * 如果你想让下拉无效，return false
     *
     * @return isScrollToTop
     */
    override val isScrollToTop: Boolean
        get() {
            if (mAdapter == null) {
                if (childCount > 1 && getChildAt(1) is ViewPager) {
                    val viewPager = getChildAt(1) as ViewPager
                    mAdapter = viewPager.adapter as FragmentAdapter?
                }
            }
            return mAdapter != null && (mAdapter!!.curFragment as PagerFragment).isScrollTop
        }
}
