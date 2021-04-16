package com.naposystems.napoleonchat.ui.multipreview.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.databinding.FragmentMultipleAttachmentPreviewImageBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentPreviewListener

class MultipleAttachmentPreviewImageFragment(
    val file: MultipleAttachmentFileItem
) : Fragment() {

    private lateinit var binding: FragmentMultipleAttachmentPreviewImageBinding
    private var listener: MultipleAttachmentPreviewListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultipleAttachmentPreviewImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.imagePreview.setOnClickListener { listener?.changeVisibilityOptions() }
        if (file.messageAndAttachment == null) {
            loadImage()
        } else {
            loadImageFromBody()
        }
    }

    private fun loadImageFromBody() {
        try {
            binding.apply {
                Glide.with(root.context)
                    .load(file.messageAndAttachment?.attachment?.body)
                    .into(imagePreview)
            }
        } catch (exception: Exception) {

        }
    }

    private fun loadImage() {
        try {
            binding.apply {
                Glide.with(root.context).load(file.contentUri)
                    .into(imagePreview)
            }
        } catch (exception: Exception) {

        }
    }

    fun setListener(listener: MultipleAttachmentPreviewListener) {
        this.listener = listener
    }

}