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

package com.cygnet.caleviewdemo.adapter.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 基本的适配器
 */
abstract class BaseRecyclerAdapter<T>(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mInflater: LayoutInflater = LayoutInflater.from(context)

    protected var mItems: MutableList<T> = ArrayList()
    private var onItemClickListener: OnItemClickListener? = null
    private val onClickListener: OnClickListener

    init {
        onClickListener = object : OnClickListener() {
            override fun onClick(position: Int, itemId: Long) {
                if (onItemClickListener != null) onItemClickListener!!.onItemClick(position, itemId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = onCreateDefaultViewHolder(parent, viewType)
        holder.itemView.tag = holder
        holder.itemView.setOnClickListener(onClickListener)
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, mItems[position], position)
    }

    protected abstract fun onCreateDefaultViewHolder(parent: ViewGroup?, type: Int): RecyclerView.ViewHolder
    protected abstract fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: T, position: Int)
    override fun getItemCount(): Int {
        return mItems.size
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    fun addAll(items: List<T>?) {
        if (!items.isNullOrEmpty()) {
            mItems.addAll(items)
            notifyItemRangeInserted(mItems.size, items.size)
        }
    }

    fun addItem(item: T?) {
        if (item != null) {
            mItems.add(item)
            notifyItemChanged(mItems.size)
        }
    }

    val items: List<T>
        get() = mItems

    fun getItem(position: Int): T? {
        return if (position < 0 || position >= mItems.size) null else mItems[position]
    }

    internal abstract class OnClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            val holder = v.tag as RecyclerView.ViewHolder
            onClick(holder.adapterPosition, holder.itemId)
        }

        abstract fun onClick(position: Int, itemId: Long)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, itemId: Long)
    }

    fun removeItem(item: T) {
        if (mItems.contains(item)) {
            val position = mItems.indexOf(item)
            mItems.remove(item)
            notifyItemRemoved(position)
        }
    }

    protected fun removeItem(position: Int) {
        if (this.itemCount > position) {
            mItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    protected fun clear() {
        mItems.clear()
        notifyDataSetChanged()
    }
}
