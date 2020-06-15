package com.naposystems.pepito.ui.attachment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationAttachmentDialogFragmentBinding

class AttachmentDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: ConversationAttachmentDialogFragmentBinding
    private lateinit var listener: OnAttachmentDialogListener

    interface OnAttachmentDialogListener {
        fun galleryPressed()
        fun cameraPressed()
        fun locationPressed()
        fun audioPressed()
        fun documentPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.conversation_attachment_dialog_fragment,
            container,
            false
        )

        binding.cardViewGallery.setOnClickListener {
            dismiss()
            listener.galleryPressed()
        }
        binding.cardViewCamera.setOnClickListener {
            dismiss()
            listener.cameraPressed()
        }
        binding.cardViewLocation.setOnClickListener {
            dismiss()
            listener.locationPressed()
        }
        binding.cardViewAudio.setOnClickListener {
            dismiss()
            listener.audioPressed()
        }
        binding.cardViewDocument.setOnClickListener {
            dismiss()
            listener.documentPressed()
        }
        binding.cardViewClose.setOnClickListener { dismiss() }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    fun setListener(listener: OnAttachmentDialogListener) {
        this.listener = listener
    }
}