package com.naposystems.napoleonchat.ui.multi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naposystems.napoleonchat.databinding.FragmentMultipleAttachmentExitDialogBinding
import com.naposystems.napoleonchat.ui.multipreview.listeners.events.MultipleAttachmentRemoveEvent

class MultipleAttachmentExitBottomSheetDialogFragment(
    val functionInAccept: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var viewBinding: FragmentMultipleAttachmentExitDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentMultipleAttachmentExitDialogBinding.inflate(
            inflater,
            container,
            false
        )
        return viewBinding.root
    }

    override fun onStart() {
        super.onStart()
        defineListeners()
    }

    private fun defineListeners() {

        viewBinding.apply {
            buttonActionPrimary.setOnClickListener {
                functionInAccept.invoke()
                dismiss()
            }
            buttonActionCancel.setOnClickListener {
                dismiss()
            }
        }

    }

}