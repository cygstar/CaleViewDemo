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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.AbsListView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.cygnet.caleviewdemo.R
import kotlin.math.abs

/**
 * 日历布局
 */
class CalendarLayout(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    /**
     * 多点触控支持
     */
    private var mActivePointerId = 0

    /**
     * 默认状态
     */
    private val mDefaultStatus: Int

    private var isWeekView = false

    /**
     * 星期栏
     */
    var mWeekBar: WeekBar? = null

    /**
     * 自定义ViewPager，月视图
     */
    private var mMonthView: MonthViewPager? = null

    /**
     * 日历
     */
    var mCalendarView: CalendarView? = null

    /**
     * 自定义的周视图
     */
    private var mWeekPager: WeekViewPager? = null

    /**
     * 年视图
     */
    private var mYearView: YearViewPager? = null

    /**
     * ContentView
     */
    var mContentView: ViewGroup? = null

    /**
     * 手势模式
     */
    private val mGestureMode: Int

    private var mCalendarShowMode: Int

    private var mContentViewTranslateY = 0 //ContentView  可滑动的最大距离距离 , 固定
    private var mViewPagerTranslateY = 0 // ViewPager可以平移的距离，不代表mMonthView的平移距离

    private var downY = 0f
    private var mLastY = 0f
    private var mLastX = 0f
    private var isAnimating = false

    /**
     * 内容布局id
     */
    private val mContentViewId: Int

    /**
     * 手速判断
     */
    private val mVelocityTracker: VelocityTracker
    private val mMaximumVelocity: Int

    private var mItemHeight = 0

    private var mDelegate: CalendarViewDelegate? = null

    init {
        orientation = VERTICAL
        val array = context.obtainStyledAttributes(attrs, R.styleable.CalendarLayout)
        mContentViewId = array.getResourceId(R.styleable.CalendarLayout_calendar_content_view_id, 0)
        mDefaultStatus = array.getInt(R.styleable.CalendarLayout_default_status, STATUS_EXPAND)
        mCalendarShowMode = array.getInt(R.styleable.CalendarLayout_calendar_show_mode, CALENDAR_SHOW_MODE_BOTH_MONTH_WEEK_VIEW)
        mGestureMode = array.getInt(R.styleable.CalendarLayout_gesture_mode, GESTURE_MODE_DEFAULT)
        array.recycle()
        mVelocityTracker = VelocityTracker.obtain()
        val configuration = ViewConfiguration.get(context)
        val mTouchSlop = configuration.scaledTouchSlop
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
    }

    /**
     * 初始化
     *
     * @param delegate delegate
     */
    fun setup(delegate: CalendarViewDelegate) {
        this.mDelegate = delegate
        mItemHeight = mDelegate!!.calendarItemHeight
        initCalendarPosition(if (delegate.mSelectedCalendar.isAvailable) delegate.mSelectedCalendar else delegate.createCurrentDate())
        updateContentViewTranslateY()
    }

    /**
     * 初始化当前时间的位置
     *
     * @param cur 当前日期时间
     */
    private fun initCalendarPosition(cur: Calendar) {
        val diff = CalendarUtil.getMonthViewStartDiff(cur, mDelegate!!.weekStart)
        val size = diff + cur.day - 1
        updateSelectPosition(size)
    }

    /**
     * 当前第几项被选中，更新平移量
     *
     * @param selectPosition 月视图被点击的position
     */
    fun updateSelectPosition(selectPosition: Int) {
        val line = (selectPosition + 7) / 7
        mViewPagerTranslateY = (line - 1) * mItemHeight
    }

    /**
     * 设置选中的周，更新位置
     *
     * @param week week
     */
    fun updateSelectWeek(week: Int) {
        mViewPagerTranslateY = (week - 1) * mItemHeight
    }

    /**
     * 更新内容ContentView可平移的最大距离
     */
    fun updateContentViewTranslateY() {
        val calendar = mDelegate!!.mIndexCalendar
        mContentViewTranslateY = if (mDelegate!!.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) {
            5 * mItemHeight
        }
        else {
            CalendarUtil.getMonthViewHeight(
                calendar.year,
                calendar.month, mItemHeight, mDelegate!!.weekStart
            ) - mItemHeight
        }
        //已经显示周视图，则需要动态平移contentView的高度
        if (mWeekPager!!.visibility == VISIBLE) {
            if (mContentView == null) return
            mContentView!!.translationY = -mContentViewTranslateY.toFloat()
        }
    }

    /**
     * 更新日历项高度
     */
    fun updateCalendarItemHeight() {
        mItemHeight = mDelegate!!.calendarItemHeight
        if (mContentView == null) return
        val calendar = mDelegate!!.mIndexCalendar
        updateSelectWeek(CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate!!.weekStart))
        mContentViewTranslateY = if (mDelegate!!.monthViewShowMode == CalendarViewDelegate.MODE_ALL_MONTH) {
            5 * mItemHeight
        }
        else {
            CalendarUtil.getMonthViewHeight(
                calendar.year, calendar.month,
                mItemHeight, mDelegate!!.weekStart
            ) - mItemHeight
        }
        translationViewPager()
        if (mWeekPager!!.visibility == VISIBLE) mContentView!!.translationY = -mContentViewTranslateY.toFloat()
    }

    /**
     * 隐藏日历
     */
    fun hideCalendarView() {
        if (mCalendarView == null) return
        mCalendarView!!.visibility = GONE
        if (!isExpand) expand(0)
        requestLayout()
    }

    /**
     * 显示日历
     */
    fun showCalendarView() {
        mCalendarView!!.visibility = VISIBLE
        requestLayout()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mGestureMode == GESTURE_MODE_DISABLED || mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW || mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW) { //禁用手势，或者只显示某种视图
            return false
        }
        if (mDelegate == null) return false
        if (mDelegate!!.isShowYearSelectedLayout) return false
        if (mContentView == null || mCalendarView == null || mCalendarView!!.visibility == GONE) return false

        val action = event.action
        val y = event.y
        mVelocityTracker.addMovement(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val index = event.actionIndex
                mActivePointerId = event.getPointerId(index)
                run {
                    downY = y
                    mLastY = downY
                }
                return true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val idx = event.actionIndex
                mActivePointerId = event.getPointerId(idx)
                if (mActivePointerId == 0) {
                    //核心代码：就是让下面的 dy = y- mLastY == 0，避免抖动
                    mLastY = event.getY(mActivePointerId)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                getPointerIndex(event, mActivePointerId)
                if (mActivePointerId == INVALID_POINTER) {
                    //如果切换了手指，那把mLastY换到最新手指的y坐标即可，核心就是让下面的 dy== 0，避免抖动
                    mLastY = y
                    mActivePointerId = ACTIVE_POINTER
                }
                val dy = y - mLastY

                //向上滑动，并且contentView平移到最大距离，显示周视图
                if (dy < 0 && mContentView!!.translationY == -mContentViewTranslateY.toFloat()) {
                    mLastY = y
                    event.action = MotionEvent.ACTION_DOWN
                    dispatchTouchEvent(event)
                    mWeekPager!!.visibility = VISIBLE
                    mMonthView!!.visibility = INVISIBLE
                    if (!isWeekView && mDelegate!!.mViewChangeListener != null) {
                        mDelegate!!.mViewChangeListener!!.onViewChange(false)
                    }
                    isWeekView = true
                    return true
                }
                hideWeek(false)

                //向下滑动，并且contentView已经完全平移到底部
                if (dy > 0 && mContentView!!.translationY + dy >= 0) {
                    mContentView!!.translationY = 0f
                    translationViewPager()
                    mLastY = y
                    return super.onTouchEvent(event)
                }

                //向上滑动，并且contentView已经平移到最大距离，则contentView平移到最大的距离
                if (dy < 0 && mContentView!!.translationY + dy <= -mContentViewTranslateY) {
                    mContentView!!.translationY = -mContentViewTranslateY.toFloat()
                    translationViewPager()
                    mLastY = y
                    return super.onTouchEvent(event)
                }
                //否则按比例平移
                mContentView!!.translationY += dy
                translationViewPager()
                mLastY = y
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = getPointerIndex(event, mActivePointerId)
                if (mActivePointerId == INVALID_POINTER) return false
                mLastY = event.getY(pointerIndex)
            }

            MotionEvent.ACTION_UP -> {
                val velocityTracker = mVelocityTracker
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val mYVelocity = velocityTracker.yVelocity
                if (mContentView!!.translationY == 0f || mContentView!!.translationY == mContentViewTranslateY.toFloat()) {
                    expand()
                    return false
                }
                if (abs(mYVelocity.toDouble()) >= 800) {
                    if (mYVelocity < 0) shrink() else expand()
                    return super.onTouchEvent(event)
                }
                if (event.y - downY > 0) expand() else shrink()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (isAnimating) return super.dispatchTouchEvent(ev)
        if (mGestureMode == GESTURE_MODE_DISABLED) return super.dispatchTouchEvent(ev)
        if (mYearView == null || mCalendarView == null || mCalendarView!!.visibility == GONE || mContentView == null || mContentView!!.visibility != VISIBLE) {
            return super.dispatchTouchEvent(ev)
        }

        if (mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW || mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW) {
            return super.dispatchTouchEvent(ev)
        }

        if (mYearView!!.visibility == VISIBLE || mDelegate!!.isShowYearSelectedLayout) {
            return super.dispatchTouchEvent(ev)
        }
        val action = ev.action
        val y = ev.y
        if (action == MotionEvent.ACTION_MOVE) {
            val dy = y - mLastY
            /*
             * 如果向下滚动，有 2 种情况处理 且y在ViewPager下方
             * 1、RecyclerView 或者其它滚动的View，当mContentView滚动到顶部时，拦截事件
             * 2、非滚动控件，直接拦截事件
             */
            if (dy > 0 && mContentView!!.translationY == -mContentViewTranslateY.toFloat()) {
                if (isScrollTop) {
                    requestDisallowInterceptTouchEvent(false) //父View向子View拦截分发事件
                    return super.dispatchTouchEvent(ev)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isAnimating) return true
        if (mGestureMode == GESTURE_MODE_DISABLED) return false
        if (mYearView == null || mCalendarView == null || mCalendarView!!.visibility == GONE || mContentView == null || mContentView!!.visibility != VISIBLE) {
            return super.onInterceptTouchEvent(ev)
        }

        if (mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW || mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW) {
            return false
        }

        if (mYearView!!.visibility == VISIBLE || mDelegate!!.isShowYearSelectedLayout) {
            return super.onInterceptTouchEvent(ev)
        }
        val action = ev.action
        val y = ev.y
        val x = ev.x
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val index = ev.actionIndex
                mActivePointerId = ev.getPointerId(index)
                run {
                    downY = y
                    mLastY = downY
                }
                mLastX = x
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = y - mLastY
                val dx = x - mLastX
                /*
                   如果向上滚动，且ViewPager已经收缩，不拦截事件
                 */
                if (dy < 0 && mContentView!!.translationY == -mContentViewTranslateY.toFloat()) {
                    return false
                }
                /*
                 * 如果向下滚动，有 2 种情况处理 且y在ViewPager下方
                 * 1、RecyclerView 或者其它滚动的View，当mContentView滚动到顶部时，拦截事件
                 * 2、非滚动控件，直接拦截事件
                 */
                if (dy > 0 && mContentView!!.translationY == -mContentViewTranslateY.toFloat() && y >= mDelegate!!.calendarItemHeight + mDelegate!!.weekBarHeight) {
                    if (!isScrollTop) return false
                }

                if (dy > 0 && mContentView!!.translationY == 0f && y >= CalendarUtil.dipToPx(context, 98f)) {
                    return false
                }

                if (abs(dy.toDouble()) > abs(dx.toDouble())) { //纵向滑动距离大于横向滑动距离,拦截滑动事件
                    if ((dy > 0 && mContentView!!.translationY <= 0) || (dy < 0 && mContentView!!.translationY >= -mContentViewTranslateY)) {
                        mLastY = y
                        return true
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun getPointerIndex(ev: MotionEvent, id: Int): Int {
        val activePointerIndex = ev.findPointerIndex(id)
        if (activePointerIndex == -1) mActivePointerId = INVALID_POINTER
        return activePointerIndex
    }

    override fun onMeasure(widthMeaSpec: Int, heightMeaSpec: Int) {
        var heightMeasureSpec = heightMeaSpec
        if (mContentView == null || mCalendarView == null) {
            super.onMeasure(widthMeaSpec, heightMeasureSpec)
            return
        }

        val year = mDelegate!!.mIndexCalendar.year
        val month = mDelegate!!.mIndexCalendar.month
        val weekBarHeight = (CalendarUtil.dipToPx(context, 1f) + mDelegate!!.weekBarHeight)

        val monthHeight = CalendarUtil.getMonthViewHeight(
            year, month,
            mDelegate!!.calendarItemHeight,
            mDelegate!!.weekStart,
            mDelegate!!.monthViewShowMode
        ) + weekBarHeight

        var height = MeasureSpec.getSize(heightMeasureSpec)

        if (mDelegate!!.isFullScreenCalendar) {
            super.onMeasure(widthMeaSpec, heightMeasureSpec)
            val heightSpec = MeasureSpec.makeMeasureSpec(
                height - weekBarHeight - mDelegate!!.calendarItemHeight,
                MeasureSpec.EXACTLY
            )
            mContentView!!.measure(widthMeaSpec, heightSpec)
            mContentView!!.layout(mContentView!!.left, mContentView!!.top, mContentView!!.right, mContentView!!.bottom)
            return
        }

        if (monthHeight >= height && mMonthView!!.height > 0) {
            height = monthHeight
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                monthHeight + weekBarHeight + mDelegate!!.weekBarHeight, MeasureSpec.EXACTLY
            )
        }
        else if (monthHeight < height && mMonthView!!.height > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        }
        val hei = if (mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW || mCalendarView!!.visibility == GONE) {
            height - (if (mCalendarView!!.visibility == GONE) 0 else mCalendarView!!.height)
        }
        else if (mGestureMode == GESTURE_MODE_DISABLED && !isAnimating) {
            if (isExpand) height - monthHeight else height - weekBarHeight - mItemHeight
        }
        else {
            height - weekBarHeight - mItemHeight
        }
        super.onMeasure(widthMeaSpec, heightMeasureSpec)
        val heightSpec = MeasureSpec.makeMeasureSpec(
            hei,
            MeasureSpec.EXACTLY
        )
        mContentView!!.measure(widthMeaSpec, heightSpec)
        mContentView!!.layout(mContentView!!.left, mContentView!!.top, mContentView!!.right, mContentView!!.bottom)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mMonthView = findViewById(R.id.vp_month)
        mWeekPager = findViewById(R.id.vp_week)
        if (childCount > 0) mCalendarView = getChildAt(0) as CalendarView
        mContentView = findViewById(mContentViewId)
        mYearView = findViewById(R.id.selectLayout)
    }

    /**
     * 平移ViewPager月视图
     */
    private fun translationViewPager() {
        val percent = mContentView!!.translationY * 1.0f / mContentViewTranslateY
        mMonthView!!.translationY = mViewPagerTranslateY * percent
    }

    fun setModeBothMonthWeekView() {
        mCalendarShowMode = CALENDAR_SHOW_MODE_BOTH_MONTH_WEEK_VIEW
        requestLayout()
    }

    fun setModeOnlyWeekView() {
        mCalendarShowMode = CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW
        requestLayout()
    }

    fun setModeOnlyMonthView() {
        mCalendarShowMode = CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW
        requestLayout()
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        val parcelable = super.onSaveInstanceState()
        bundle.putParcelable("super", parcelable)
        bundle.putBoolean("isExpand", isExpand)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        val superData = bundle.getParcelable<Parcelable>("super")
        val isExpand = bundle.getBoolean("isExpand")
        if (isExpand) post { expand(0) } else post { shrink(0) }
        super.onRestoreInstanceState(superData)
    }

    val isExpand: Boolean
        /**
         * 是否展开了
         *
         * @return isExpand
         */
        get() = mMonthView!!.visibility == VISIBLE

    /**
     * 展开
     *
     * @param duration 时长
     * @return 展开是否成功
     */
    fun expand(duration: Int = 240): Boolean {
        if (isAnimating || mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW || mContentView == null) return false
        if (mMonthView!!.visibility != VISIBLE) {
            mWeekPager!!.visibility = GONE
            onShowMonthView()
            isWeekView = false
            mMonthView!!.visibility = VISIBLE
        }
        val objectAnimator = ObjectAnimator.ofFloat(mContentView, "translationY", mContentView!!.translationY, 0f)
        objectAnimator.setDuration(duration.toLong())
        objectAnimator.addUpdateListener { animation ->
            val currentValue = animation.animatedValue as Float
            val percent = currentValue * 1.0f / mContentViewTranslateY
            mMonthView!!.translationY = mViewPagerTranslateY * percent
            isAnimating = true
        }
        objectAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isAnimating = false
                if (mGestureMode == GESTURE_MODE_DISABLED) requestLayout()
                hideWeek(true)
                if (mDelegate!!.mViewChangeListener != null && isWeekView) {
                    mDelegate!!.mViewChangeListener!!.onViewChange(true)
                }
                isWeekView = false
            }
        })
        objectAnimator.start()
        return true
    }

    /**
     * 收缩
     *
     * @param duration 时长
     * @return 成功或者失败
     */
    fun shrink(duration: Int = 240): Boolean {
        if (mGestureMode == GESTURE_MODE_DISABLED) {
            requestLayout()
        }
        if (isAnimating || mContentView == null) {
            return false
        }
        val objectAnimator = ObjectAnimator.ofFloat(mContentView, "translationY", mContentView!!.translationY, -mContentViewTranslateY.toFloat())
        objectAnimator.setDuration(duration.toLong())
        objectAnimator.addUpdateListener { animation ->
            val currentValue = animation.animatedValue as Float
            val percent = currentValue * 1.0f / mContentViewTranslateY
            mMonthView!!.translationY = mViewPagerTranslateY * percent
            isAnimating = true
        }
        objectAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                isAnimating = false
                showWeek()
                isWeekView = true
            }
        })
        objectAnimator.start()
        return true
    }

    /**
     * 初始化状态
     */
    fun initStatus() {
        if ((mDefaultStatus == STATUS_SHRINK || mCalendarShowMode == CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW) && mCalendarShowMode != CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW) {
            if (mContentView == null) {
                mWeekPager!!.visibility = VISIBLE
                mMonthView!!.visibility = GONE
                return
            }
            post {
                val objectAnimator = ObjectAnimator.ofFloat(mContentView, "translationY", mContentView!!.translationY, -mContentViewTranslateY.toFloat())
                objectAnimator.setDuration(0)
                objectAnimator.addUpdateListener { animation ->
                    val currentValue = animation.animatedValue as Float
                    val percent = currentValue * 1.0f / mContentViewTranslateY
                    mMonthView!!.translationY = mViewPagerTranslateY * percent
                    isAnimating = true
                }
                objectAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        isAnimating = false
                        isWeekView = true
                        showWeek()
                        if (mDelegate == null || mDelegate!!.mViewChangeListener == null) return
                        mDelegate!!.mViewChangeListener!!.onViewChange(false)
                    }
                })
                objectAnimator.start()
            }
        }
        else {
            if (mDelegate!!.mViewChangeListener == null) return
            post { mDelegate!!.mViewChangeListener!!.onViewChange(true) }
        }
    }

    /**
     * 隐藏周视图
     */
    private fun hideWeek(isNotify: Boolean) {
        if (isNotify) onShowMonthView()
        mWeekPager!!.visibility = GONE
        mMonthView!!.visibility = VISIBLE
    }

    /**
     * 显示周视图
     */
    private fun showWeek() {
        onShowWeekView()
        if (mWeekPager != null && mWeekPager!!.adapter != null) {
            mWeekPager!!.adapter!!.notifyDataSetChanged()
            mWeekPager!!.visibility = VISIBLE
        }
        mMonthView!!.visibility = INVISIBLE
    }

    /**
     * 周视图显示事件
     */
    private fun onShowWeekView() {
        if (mWeekPager!!.visibility == VISIBLE) return
        if (mDelegate != null && mDelegate!!.mViewChangeListener != null && !isWeekView) {
            mDelegate!!.mViewChangeListener!!.onViewChange(false)
        }
    }

    /**
     * 周视图显示事件
     */
    private fun onShowMonthView() {
        if (mMonthView!!.visibility == VISIBLE) return
        if (mDelegate != null && mDelegate!!.mViewChangeListener != null && isWeekView) {
            mDelegate!!.mViewChangeListener!!.onViewChange(true)
        }
    }

    private val isScrollTop: Boolean
        /**
         * ContentView是否滚动到顶部 如果完全不适合，就复写这个方法
         *
         * @return 是否滚动到顶部
         */
        get() {
            if (mContentView is CalendarScrollView) return (mContentView as CalendarScrollView).isScrollToTop
            if (mContentView is RecyclerView) return (mContentView as RecyclerView).computeVerticalScrollOffset() == 0
            if (mContentView is AbsListView) {
                var result = false
                val listView = mContentView as AbsListView
                if (listView.firstVisiblePosition == 0) {
                    val topChildView = listView.getChildAt(0)
                    result = topChildView.top == 0
                }
                return result
            }
            return mContentView!!.scrollY == 0
        }

    /**
     * 隐藏内容布局
     */
    fun hideContentView() {
        if (mContentView == null) return
        mContentView!!.animate()
                .translationY((height - mMonthView!!.height).toFloat())
                .setDuration(220)
                .setInterpolator(LinearInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mContentView!!.visibility = INVISIBLE
                        mContentView!!.clearAnimation()
                    }
                })
    }

    /**
     * 显示内容布局
     */
    fun showContentView() {
        if (mContentView == null) return
        mContentView!!.translationY = (height - mMonthView!!.height).toFloat()
        mContentView!!.visibility = VISIBLE
        mContentView!!.animate()
                .translationY(0f)
                .setDuration(180)
                .setInterpolator(LinearInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                })
    }

    private val calendarViewHeight: Int
        get() = if (mMonthView!!.visibility == VISIBLE) mDelegate!!.weekBarHeight + mMonthView!!.height else mDelegate!!.weekBarHeight + mDelegate!!.calendarItemHeight

    /**
     * 如果有十分特别的ContentView，可以自定义实现这个接口
     */
    interface CalendarScrollView {
        /**
         * 是否滚动到顶部
         *
         * @return 是否滚动到顶部
         */
        val isScrollToTop: Boolean
    }

    companion object {
        private const val ACTIVE_POINTER = 1
        private const val INVALID_POINTER = -1

        /**
         * 周月视图
         */
        private const val CALENDAR_SHOW_MODE_BOTH_MONTH_WEEK_VIEW = 0

        /**
         * 仅周视图
         */
        private const val CALENDAR_SHOW_MODE_ONLY_WEEK_VIEW = 1

        /**
         * 仅月视图
         */
        private const val CALENDAR_SHOW_MODE_ONLY_MONTH_VIEW = 2

        /**
         * 默认展开
         */
        private const val STATUS_EXPAND = 0

        /**
         * 默认收缩
         */
        private const val STATUS_SHRINK = 1

        /**
         * 默认手势
         */
        private const val GESTURE_MODE_DEFAULT = 0

        //       /**
        //     * 仅日历有效
        //     */
        //    private static final int GESTURE_MODE_ONLY_CALENDAR = 1;
        /**
         * 禁用手势
         */
        private const val GESTURE_MODE_DISABLED = 2
    }
}
