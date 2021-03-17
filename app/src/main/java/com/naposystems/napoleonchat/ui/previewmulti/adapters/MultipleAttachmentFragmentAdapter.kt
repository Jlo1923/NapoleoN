package com.naposystems.napoleonchat.ui.previewmulti.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.previewmulti.MultipleAttachmentPreviewActivity
import com.naposystems.napoleonchat.ui.previewmulti.fragments.MultipleAttachmentPreviewImageFragment
import com.naposystems.napoleonchat.ui.previewmulti.listeners.MultipleAttachmentPreviewImageListener
import java.util.ArrayList

class MultipleAttachmentFragmentAdapter(
    private val activityParent: AppCompatActivity,
    private val items: ArrayList<MultipleAttachmentFileItem>
) : FragmentStateAdapter(activityParent) {

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return MultipleAttachmentPreviewImageFragment(items[position]).apply {
            this.setListener(activityParent as MultipleAttachmentPreviewImageListener)
        }
    }

}