package com.cygnet.caleviewdemo.adapter.group

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.cygnet.caleviewdemo.R

/**
 * 带分组浮动的RecyclerView
 * Created by haibin on 2017/5/15.
 */
class GroupRecyclerView : RecyclerView {

    private lateinit var mItemDecoration: GroupItemDecoration<*, *>
    private lateinit var mListener: OnGroupChangeListener

    private var mGroupHeight = 0
    private var mGroutBackground = 0
    private var mTextColor = 0
    private var mTextSize = 0
    private var mPaddingLeft = 0
    private var mPaddingRight = 0
    private var isCenter = false
    private var mChildItemOffset = 0
    private var isHasHeader = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.GroupRecyclerView)
        mTextSize = array.getDimensionPixelSize(R.styleable.GroupRecyclerView_group_text_size, 16)
        mGroupHeight = array.getDimension(R.styleable.GroupRecyclerView_group_height, 52f).toInt()
        mChildItemOffset = array.getDimension(R.styleable.GroupRecyclerView_group_child_offset, 20f).toInt()
        mTextColor = array.getColor(R.styleable.GroupRecyclerView_group_text_color, -0x1)
        mGroutBackground = array.getColor(R.styleable.GroupRecyclerView_group_background, -0x80000000)
        isCenter = array.getBoolean(R.styleable.GroupRecyclerView_group_center, false)
        isHasHeader = array.getBoolean(R.styleable.GroupRecyclerView_group_has_header, true)
        mPaddingLeft = array.getDimension(R.styleable.GroupRecyclerView_group_padding_left, 16f).toInt()
        mPaddingRight = array.getDimension(R.styleable.GroupRecyclerView_group_padding_right, 16f).toInt()
        array.recycle()
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (adapter is GroupRecyclerAdapter<*, *>) super.setAdapter(adapter)
        else throw IllegalStateException("Adapter must instance of GroupRecyclerAdapter or extends GroupRecyclerAdapter")
    }

    override fun addItemDecoration(decor: ItemDecoration) {
        if (decor is GroupItemDecoration<*, *>) super.addItemDecoration(decor)
        else throw IllegalStateException("ItemDecoration must instance of GroupItemDecoration or extends GroupItemDecoration")
        mItemDecoration = decor
        mItemDecoration.setTextSize(mTextSize.toFloat())
        mItemDecoration.setBackground(mGroutBackground)
        mItemDecoration.setTextColor(mTextColor)
        mItemDecoration.setGroupHeight(mGroupHeight)
        mItemDecoration.setPadding(mPaddingLeft, mPaddingRight)
        mItemDecoration.setCenter(isCenter)
        mItemDecoration.setHasHeader(isHasHeader)
        mItemDecoration.setChildItemOffset(mChildItemOffset)
        //mItemDecoration.notifyDataSetChanged((GroupRecyclerAdapter) getAdapter());
    }

    fun notifyDataSetChanged() {
        mItemDecoration.notifyDataSetChanged(adapter as GroupRecyclerAdapter<*, *>)
    }

    fun setOnGroupChangeListener(listener: OnGroupChangeListener) {
        mListener = listener
    }

    /**
     * 分组最上面改变通知
     */
    interface OnGroupChangeListener {
        fun onGroupChange(groupPosition: Int, group: String)
    }
}
