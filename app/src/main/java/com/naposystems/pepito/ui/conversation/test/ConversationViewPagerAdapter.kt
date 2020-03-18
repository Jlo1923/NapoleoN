package com.naposystems.pepito.ui.conversation.test

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class ConversationViewPagerAdapter constructor(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private var mListFragments: MutableList<Fragment> = arrayListOf()

    override fun getItemCount() = mListFragments.size

    override fun createFragment(position: Int) = mListFragments[position]

    fun addFragment(fragment: Fragment) {
        mListFragments.add(fragment)
    }
}