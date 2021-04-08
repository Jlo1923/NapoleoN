package com.naposystems.napoleonchat.ui.multipreview.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naposystems.napoleonchat.databinding.FragmentMultipleAttachmentRemoveAttachmentDialogBinding
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentRemoveListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.events.MultipleAttachmentRemoveEvent
import com.naposystems.napoleonchat.ui.multipreview.model.MultipleAttachmentRemoveItem
import com.naposystems.napoleonchat.utility.extensions.hide

class MultipleAttachmentRemoveAttachmentDialogFragment(
    private val itemTexts: MultipleAttachmentRemoveItem,
    val listener: MultipleAttachmentRemoveListener
) : BottomSheetDialogFragment() {

    private lateinit var viewBinding: FragmentMultipleAttachmentRemoveAttachmentDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentMultipleAttachmentRemoveAttachmentDialogBinding.inflate(
            inflater,
            container,
            false
        )
        return viewBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        writeTexts()
        defineListeners()
    }

    private fun writeTexts() {
        viewBinding.apply {
            textTitle.text = itemTexts.title
            textMessage.text = itemTexts.message
            buttonActionPrimary.text = itemTexts.option1
            itemTexts.option2?.let {
                buttonActionSecondary.text = itemTexts.option2
            } ?: run { buttonActionSecondary.hide() }
            buttonActionCancel.text = itemTexts.cancelText
        }
    }

    private fun defineListeners() {

        viewBinding.apply {
            buttonActionPrimary.setOnClickListener {
                listener.onRemoveAttachment(MultipleAttachmentRemoveEvent.OnSimpleRemove)
                dismiss()
            }
            buttonActionCancel.setOnClickListener {
                dismiss()
            }
        }

    }

}