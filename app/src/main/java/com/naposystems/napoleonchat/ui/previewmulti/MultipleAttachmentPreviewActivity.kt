package com.naposystems.napoleonchat.ui.previewmulti

import android.content.ContentUris
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.naposystems.napoleonchat.databinding.ActivityMultipleAttachmentPreviewBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.previewmulti.adapters.MultipleAttachmentFragmentAdapter
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewAction
import com.naposystems.napoleonchat.ui.previewmulti.fragments.MultipleAttachmentPreviewImageFragment
import com.naposystems.napoleonchat.ui.previewmulti.listeners.MultipleAttachmentPreviewImageListener
import com.naposystems.napoleonchat.ui.previewmulti.views.ViewMultipleAttachmentTabView
import com.naposystems.napoleonchat.ui.previewmulti.listeners.ViewAttachmentOptionsListener
import com.naposystems.napoleonchat.utility.anims.animHideSlideDown
import com.naposystems.napoleonchat.utility.anims.animHideSlideUp
import com.naposystems.napoleonchat.utility.anims.animShowSlideDown
import com.naposystems.napoleonchat.utility.anims.animShowSlideUp
import com.naposystems.napoleonchat.utility.extensions.showViews
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.AndroidInjection
import javax.inject.Inject

class MultipleAttachmentPreviewActivity
    : AppCompatActivity(),
    ViewAttachmentOptionsListener,
    MultipleAttachmentPreviewImageListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: MultipleAttachmentPreviewViewModel

    private lateinit var viewBinding: ActivityMultipleAttachmentPreviewBinding

    private var adapter: MultipleAttachmentFragmentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MultipleAttachmentPreviewViewModel::class.java)

        lifecycle.addObserver(viewModel)

        viewBinding = ActivityMultipleAttachmentPreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

    }

    override fun onStart() {
        super.onStart()
        extractFilesFromExtras()
        bindViewModel()
        defineListeners()
    }

    private fun extractFilesFromExtras() {
        val extras = intent.extras
        extras?.let { bundle ->
            val files = bundle.getParcelableArrayList<MultipleAttachmentFileItem>("test")
            files?.let {
                adapter = MultipleAttachmentFragmentAdapter(this, it)
                viewBinding.apply {

                    viewPagerAttachments.offscreenPageLimit = 2
                    viewPagerAttachments.adapter = adapter

                    TabLayoutMediator(
                        viewPreviewBottom.getTabLayout(),
                        viewPagerAttachments
                    ) { tab, position ->
                        val view = ViewMultipleAttachmentTabView(viewBinding.root.context)
                        view.bindFile(it[position])
                        view.selected(position == 0)
                        tab.customView = view
                    }.attach()

                }
                configureTabsAndViewPager()
            }
        }
    }

    private fun bindViewModel() {
        viewModel.actions().observe(this, { handleActions(it) })
    }

    private fun handleActions(action: MultipleAttachmentPreviewAction) {
        when (action) {
            MultipleAttachmentPreviewAction.Exit -> TODO()
            MultipleAttachmentPreviewAction.HideAttachmentOptions -> hideAttachmentOptions()
            MultipleAttachmentPreviewAction.ShowAttachmentOptions -> showAttachmentOptions()
            is MultipleAttachmentPreviewAction.ShowSelectFolderName -> TODO()
        }

    }

    private fun configureTabsAndViewPager() {

        viewBinding.viewPreviewBottom.getTabLayout()
            .addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab) =
                    (tab.customView as ViewMultipleAttachmentTabView).selected(true)

                override fun onTabUnselected(tab: TabLayout.Tab) =
                    (tab.customView as ViewMultipleAttachmentTabView).selected(false)

                override fun onTabReselected(tab: TabLayout.Tab) {
                }

            })

        viewBinding.viewPagerAttachments.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //showAttachmentOptionsWithoutAnim()
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
    }


    private fun defineListeners() {
        viewBinding.apply {
            imageClose.setOnClickListener { finish() }
            viewAttachmentOptions.defineListener(this@MultipleAttachmentPreviewActivity)
        }
    }

    override fun onChangeSelfDestruction() {
        Toast.makeText(viewBinding.root.context, "onChangeSelfDestruction", Toast.LENGTH_SHORT)
            .show()
    }

    private fun hideAttachmentOptions() {
        viewBinding.apply {
            imageClose.animHideSlideUp()
            viewAttachmentOptions.animHideSlideUp()
            viewPreviewBottom.animHideSlideDown()
        }
    }

    private fun showAttachmentOptions() {
        viewBinding.apply {
            imageClose.animShowSlideDown()
            viewAttachmentOptions.animShowSlideDown()
            viewPreviewBottom.animShowSlideUp()
        }
    }

    private fun showAttachmentOptionsWithoutAnim() {
        viewBinding.apply {
            showViews(imageClose, viewAttachmentOptions, viewPreviewBottom)
        }
    }

    override fun changeVisibilityOptions() {
        viewModel.changeVisibilityOptions()
    }

}