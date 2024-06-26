package com.cygnet.caleviewdemo.adapter.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    private lateinit var mInflater: LayoutInflater

    var mRootView: View? = null
    var mContext: Context? = null

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mRootView != null) {
            val parent = mRootView?.parent as? ViewGroup
            parent?.removeView(mRootView)
        }
        else {
            mRootView = inflater.inflate(layoutId, container, false)
            mInflater = inflater
            initView()
            initData()
        }
        return mRootView
    }

    override fun onDetach() {
        mContext = null
        super.onDetach()
    }

    protected abstract val layoutId: Int
    protected abstract fun initView()
    protected abstract fun initData()
}
