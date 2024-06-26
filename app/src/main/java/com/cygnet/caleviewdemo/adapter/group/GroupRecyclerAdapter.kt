package com.cygnet.caleviewdemo.adapter.group

import android.content.Context
import com.cygnet.caleviewdemo.adapter.base.BaseRecyclerAdapter

/**
 * 分组的RecyclerAdapter
 * Created by huanghaibin on 2017/5/15.
 */
abstract class GroupRecyclerAdapter<Parent, Child>(context: Context) : BaseRecyclerAdapter<Child>(context) {

    private val mGroups: LinkedHashMap<Parent, MutableList<Child>> = LinkedHashMap()
    private val mGroupTitles: MutableList<Parent> = ArrayList()

    /**
     * 返回特定的标题
     */
    fun getGroup(groupPosition: Int): Parent {
        return mGroupTitles[groupPosition]
    }

    val groupCount: Int
        /**
         * 获得分组的数量
         *
         * @return 组的数量
         */
        get() = mGroupTitles.size

    /**
     * 获取某一组的数量
     *
     * @param groupPosition groupPosition
     * @return 某一组的数量
     */
    fun getChildCount(groupPosition: Int): Int {
        if (mGroups.size == 0) return 0
        return if (mGroups[mGroupTitles[groupPosition]] == null) 0 else mGroups[mGroupTitles[groupPosition]]!!.size
    }

    /**
     * 重置分组数据
     *
     * @param groups groups
     * @param titles titles
     */
    protected fun resetGroups(groups: LinkedHashMap<Parent, MutableList<Child>>, titles: List<Parent>?) {
        if (titles == null) {
            return
        }
        mGroups.clear()
        mGroupTitles.clear()
        mGroups.putAll(groups)
        mGroupTitles.addAll(titles)
        mItems.clear()
        for (key in mGroups.keys) {
            mItems.addAll(mGroups[key]!!)
        }
        notifyDataSetChanged()
    }

    /**
     * 清除分组数据
     */
    fun clearGroup() {
        mGroupTitles.clear()
        mGroups.clear()
        clear()
    }

    /**
     * 从分组移除数据
     *
     * @param position 下标
     * @return 分组是否为空，要移除分组
     */
    fun removeGroupItem(position: Int): Boolean {
        val group = getGroupIndex(position)
        removeGroupChildren(group)
        val count = getChildCount(group)
        removeItem(position)
        if (count <= 0) {
            mGroupTitles.removeAt(group)
            return true
        }
        return false
    }

    /**
     * 获取所在分组
     *
     * @param position 下标
     * @return 获取所在分组
     */
    private fun getGroupIndex(position: Int): Int {
        var count = 0
        if (position <= 0) return 0
        for ((i, parent) in mGroups.keys.withIndex()) {
            count += mGroups[parent]!!.size
            if (position < count) return i
        }
        return 0
    }

    private fun removeGroupChildren(groupPosition: Int) {
        if (groupPosition >= mGroupTitles.size) return
        val childList = mGroups[mGroupTitles[groupPosition]]
        if (!childList.isNullOrEmpty()) childList.removeAt(childList.size - 1)
    }
}
