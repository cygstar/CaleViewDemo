package com.cygnet.caleviewdemo.adapter.group

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * 分组浮动的ItemDecoration
 * Created by huanghaibin on 2017/5/15.
 */
class GroupItemDecoration<Group, Child> : ItemDecoration() {

    private lateinit var mBackgroundPaint: Paint
    private lateinit var mTextPaint: Paint

    private var mGroup: MutableMap<Int, Group> = HashMap()

    private var mGroupHeight = 0
    private var mTextBaseLine = 0f
    private var mPaddingLeft = 0
    private var mPaddingRight = 0
    private var isCenter = false
    private var isHasHeader = false
    private var mChildItemOffset = 0
    private var mGroutBackground = 0

    init {
        init()
    }

    private fun init() {
        mBackgroundPaint = Paint()
        mBackgroundPaint.color = -0xa0808
        mBackgroundPaint.style = Paint.Style.FILL
        mBackgroundPaint.isAntiAlias = true

        mTextPaint = Paint()
        mTextPaint.color = -0xcacacb
        mTextPaint.isAntiAlias = true
    }

    /**
     * 先于RecyclerView的Item onDraw调用
     *
     * @param c      RecyclerView canvas
     * @param parent RecyclerView
     * @param state  stare
     */
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)
        onDrawGroup(canvas, parent)
    }

    /**
     * 绘制分组Group
     *
     * @param c      Canvas
     * @param parent RecyclerView
     */
    private fun onDrawGroup(canvas: Canvas, parent: RecyclerView) {
        val paddingLeft = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        var top: Int
        var bottom: Int
        val count = parent.childCount
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val key = params.viewLayoutPosition
            if (mGroup.containsKey(key)) {
                top = child.top - params.topMargin - mGroupHeight
                bottom = top + mGroupHeight
                canvas.drawRect(paddingLeft.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mBackgroundPaint)
                val group = mGroup[params.viewLayoutPosition].toString()
                val y = top + mTextBaseLine
                val x: Float = if (isCenter) parent.measuredWidth / 2 - getTextX(group) else mPaddingLeft.toFloat()
                canvas.drawText(group, x, y, mTextPaint)
            }
        }
    }

    /**
     * 后于RecyclerView的Item onDraw调用
     *
     * @param c      RecyclerView canvas
     * @param parent RecyclerView
     * @param state  stare
     */
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)
        onDrawOverGroup(canvas, parent)
    }

    /**
     * 绘制悬浮组
     *
     * @param c      Canvas
     * @param parent RecyclerView
     */
    private fun onDrawOverGroup(canvas: Canvas, parent: RecyclerView) {
        val firstVisiblePosition = (parent.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        if (firstVisiblePosition == RecyclerView.NO_POSITION) return
        val group = getCroup(firstVisiblePosition) ?: return
        val groupTitle = group.toString()
        if (TextUtils.isEmpty(groupTitle)) return
        var isRestore = false
        val nextGroup = getCroup(firstVisiblePosition + 1)
        if (nextGroup != null && group != nextGroup) {
            //说明是当前组最后一个元素，但不一定碰撞了
            val child = parent.findViewHolderForAdapterPosition(firstVisiblePosition)!!.itemView
            if (child.top + child.measuredHeight < mGroupHeight) {
                //进一步检测碰撞
                canvas.save() //保存画布当前的状态
                isRestore = true
                canvas.translate(0f, (child.top + child.measuredHeight - mGroupHeight).toFloat())
            }
        }
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val top = parent.paddingTop
        val bottom = top + mGroupHeight
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mBackgroundPaint)
        val y = top + mTextBaseLine
        val x: Float = if (isCenter) parent.measuredWidth / 2 - getTextX(groupTitle) else mPaddingLeft.toFloat()
        canvas.drawText(groupTitle, x, y, mTextPaint)
        if (isRestore) canvas.restore()  //还原画布为初始状态
    }

    /**
     * 设置item的上下左右偏移量
     *
     * @param outRect rect
     * @param view    item
     * @param parent  RecyclerView
     * @param state   stare
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        getItemOffsets(outRect, view, parent, parent.getChildViewHolder(view).adapterPosition)
    }

    /**
     * 设置item的上下左右偏移量，不做任何处理就是默认状态
     *
     * @param outRect         outRect
     * @param view            view
     * @param parent          RecyclerView
     * @param adapterPosition position
     */
    private fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, adapterPosition: Int) {
        if (mGroup.containsKey(adapterPosition)) {
            outRect[0, mGroupHeight, 0] = if (mGroup.containsKey(adapterPosition + 1)) 0 else mChildItemOffset
        }
        else {
            outRect[0, 0, 0] = if (mGroup.containsKey(adapterPosition + 1)) 0 else mChildItemOffset
        }
    }

    /**
     * 获得当前ViewPosition所在的组
     *
     * @param position 当前View的position
     * @return 当前ViewPosition所在的组
     */
    private fun getCroup(pos: Int): Group? {
        var position = pos
        while (position >= 0) {
            if (mGroup.containsKey(position)) return mGroup[position]
            position--
        }
        return null
    }

    /**
     * 通知更新分组信息
     *
     * @param adapter GroupRecyclerAdapter
     */
    fun notifyDataSetChanged(adapter: GroupRecyclerAdapter<*, *>?) {
        mGroup.clear()
        if (adapter == null) return
        var key = 0
        for (i in 0 until adapter.groupCount) {
            if (i == 0) {
                mGroup[if (isHasHeader) 1 else 0] = adapter.getGroup(i) as Group
                key += adapter.getChildCount(i) + if (isHasHeader) 1 else 0
            }
            else {
                mGroup[key] = adapter.getGroup(i) as Group
                key += adapter.getChildCount(i)
            }
        }
    }

    fun setChildItemOffset(childItemOffset: Int) {
        mChildItemOffset = childItemOffset
    }

    fun setBackground(groupBackground: Int) {
        mBackgroundPaint.color = groupBackground
    }

    fun setTextColor(textColor: Int) {
        mTextPaint.color = textColor
    }

    fun setTextSize(textSize: Float) {
        mTextPaint.textSize = textSize
        val metrics = mTextPaint.fontMetrics
        mTextBaseLine = mGroupHeight / 2 - metrics.descent + (metrics.bottom - metrics.top) / 2
    }

    fun setGroupHeight(groupHeight: Int) {
        mGroupHeight = groupHeight
        val metrics = mTextPaint.fontMetrics
        mTextBaseLine = mGroupHeight / 2 - metrics.descent + (metrics.bottom - metrics.top) / 2
    }

    fun setPadding(mPaddingLeft: Int, mPaddingRight: Int) {
        this.mPaddingLeft = mPaddingLeft
        this.mPaddingRight = mPaddingRight
    }

    fun setCenter(isCenter: Boolean) {
        this.isCenter = isCenter
    }

    fun setHasHeader(hasHeader: Boolean) {
        isHasHeader = hasHeader
    }

    /**
     * 获取文本的x坐标起点
     *
     * @param str 文本
     * @return x
     */
    private fun getTextX(str: String): Float {
        val bounds = Rect()
        mTextPaint.getTextBounds(str, 0, str.length, bounds)
        return (bounds.width() / 2).toFloat()
    }

    /**
     * 获取文本的长度像素
     * @param str 文本
     * @return px
     */
    private fun getTextLengthPx(str: String): Float {
        val bounds = Rect()
        mTextPaint.getTextBounds(str, 0, str.length, bounds)
        return bounds.width().toFloat()
    }
}
