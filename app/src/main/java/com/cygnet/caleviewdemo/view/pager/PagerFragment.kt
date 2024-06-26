package com.cygnet.caleviewdemo.view.pager

import androidx.recyclerview.widget.LinearLayoutManager
import com.cygnet.caleviewdemo.R
import com.cygnet.caleviewdemo.adapter.Article
import com.cygnet.caleviewdemo.adapter.ArticleAdapter
import com.cygnet.caleviewdemo.adapter.base.BaseFragment
import com.cygnet.caleviewdemo.adapter.group.GroupItemDecoration
import com.cygnet.caleviewdemo.adapter.group.GroupRecyclerView

class PagerFragment : BaseFragment() {
    private var mRecyclerView: GroupRecyclerView? = null

    override val layoutId: Int
        get() = R.layout.fragment_pager

    override fun initView() {
        mRecyclerView = mRootView!!.findViewById(R.id.recyclerView)
        mRecyclerView?.setLayoutManager(LinearLayoutManager(mContext))

        mRecyclerView?.addItemDecoration(GroupItemDecoration<String, Article>())
        mRecyclerView?.adapter = ArticleAdapter(mContext!!)
        mRecyclerView?.notifyDataSetChanged()
    }

    override fun initData() {}

    val isScrollTop: Boolean
        get() = mRecyclerView != null && mRecyclerView!!.computeVerticalScrollOffset() == 0

    companion object {
        fun newInstance(): PagerFragment {
            return PagerFragment()
        }
    }
}
