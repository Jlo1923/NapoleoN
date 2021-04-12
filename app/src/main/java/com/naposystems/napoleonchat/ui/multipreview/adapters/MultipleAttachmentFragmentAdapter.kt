package com.naposystems.napoleonchat.ui.multipreview.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.fragments.MultipleAttachmentPreviewImageFragment
import com.naposystems.napoleonchat.ui.multipreview.fragments.MultipleAttachmentPreviewVideoFragment
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentPreviewListener
import com.naposystems.napoleonchat.utility.extensions.isVideo
import java.util.ArrayList

class MultipleAttachmentFragmentAdapter(
    private val activityParent: AppCompatActivity,
    private val items: ArrayList<MultipleAttachmentFileItem>
) : FragmentStateAdapter(activityParent) {

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return if (items[position].isVideo()) {
            MultipleAttachmentPreviewVideoFragment(items[position]).apply {
                this.setListener(activityParent as MultipleAttachmentPreviewListener)
            }
        } else {
            MultipleAttachmentPreviewImageFragment(items[position]).apply {
                this.setListener(activityParent as MultipleAttachmentPreviewListener)
            }
        }
    }

    fun removeFragment(position: Int) {
        items.removeAt(position)
        notifyItemRangeChanged(position, items.size)
        notifyDataSetChanged()
    }

}