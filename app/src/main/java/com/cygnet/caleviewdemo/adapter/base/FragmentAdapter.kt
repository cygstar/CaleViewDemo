package com.cygnet.caleviewdemo.adapter.base

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class FragmentAdapter(private val mFragmentManager: FragmentManager) : FragmentPagerAdapter(mFragmentManager) {

    var curFragment: Fragment? = null

    private lateinit var mTitles: Array<String>

    private val mFragment: MutableList<Fragment> = ArrayList()
    private var isUpdateFlag = false

    override fun getItemPosition(obj: Any): Int {
        return POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (isUpdateFlag) {
            var fragment = super.instantiateItem(container, position) as Fragment
            val tag = fragment.tag
            val transaction = mFragmentManager.beginTransaction()
            transaction.remove(fragment)
            fragment = getItem(position)
            if (!fragment.isAdded) {
                transaction.add(container.id, fragment, tag)
                        .attach(fragment)
                        .commitAllowingStateLoss()
            }
            return fragment
        }
        return super.instantiateItem(container, position)
    }

    fun reset(fragments: List<Fragment>) {
        mFragment.clear()
        mFragment.addAll(fragments)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        super.setPrimaryItem(container, position, obj)
        if (obj is Fragment) curFragment = obj
    }

    fun reset(titles: Array<String>) {
        mTitles = titles
    }

    override fun getItem(position: Int): Fragment {
        return mFragment[position]
    }

    override fun getCount(): Int {
        return mFragment.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mTitles[position]
    }
}
