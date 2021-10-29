package com.naposystems.napoleonchat.ui.home

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.FragmentTabsPagerBinding
import com.naposystems.napoleonchat.ui.home.adapter.TabsPagerAdapter
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.add_contact_friend_item.*
import kotlinx.android.synthetic.main.fragment_tabs_pager.*
import kotlinx.android.synthetic.main.fragment_tabs_pager.view.*
import javax.inject.Inject

class TabsPagerFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: HomeViewModel by viewModels { viewModelFactory }
    private lateinit var binding: FragmentTabsPagerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_tabs_pager, container, false
        )
        binding.root.tab_layout.setSelectedTabIndicatorColor(Color.WHITE)
        binding.root.tab_layout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        binding.root.tab_layout.tabTextColors = ContextCompat.getColorStateList(requireContext(), android.R.color.white)

        val numberOfTabs = 3
        binding.root.tab_layout.tabMode = TabLayout.MODE_FIXED

        binding.root.tab_layout.isInlineLabel = true

        val adapter = TabsPagerAdapter(requireActivity().supportFragmentManager, lifecycle, numberOfTabs)
        binding.root.tabs_viewpager.adapter = adapter

        // Enable Swipe
        binding.root.tabs_viewpager.isUserInputEnabled = true

        TabLayoutMediator(binding.root.tab_layout, binding.root.tabs_viewpager) { tab, position ->
            when (position) {
                0 -> {
                    //tab.text = "Chats"
                    tab.setIcon(R.drawable.ic_chat)
                }
                1 -> {
                   // tab.text = "Contactos"
                    tab.setIcon(R.drawable.ic_contacts)

                }
                2 -> {
                  //  tab.text = "Grupos"
                    tab.setIcon(R.drawable.ic_groups)

                }

            }
            tab.icon?.colorFilter =
                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    Color.WHITE,
                    BlendModeCompat.SRC_ATOP
                )
        }.attach()


        setCustomTabTitles()
        return binding.root
    }
    private fun setCustomTabTitles() {
        val vg = binding.root.tab_layout.getChildAt(0) as ViewGroup
        val tabsCount = vg.childCount

        for (j in 0 until tabsCount) {
            val vgTab = vg.getChildAt(j) as ViewGroup

            val tabChildCount = vgTab.childCount

            for (i in 0 until tabChildCount) {
                val tabViewChild = vgTab.getChildAt(i)
                if (tabViewChild is TextView) {
                    tabViewChild.typeface = Typeface.DEFAULT_BOLD
                }
            }
        }
    }
        override fun onAttach(context: Context) {
            AndroidSupportInjection.inject(this)
            super.onAttach(context)
        }
}