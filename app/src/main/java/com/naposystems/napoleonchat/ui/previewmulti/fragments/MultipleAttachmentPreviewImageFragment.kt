package com.naposystems.napoleonchat.ui.previewmulti.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ConversationFragmentBinding
import com.naposystems.napoleonchat.databinding.FragmentMultipleAttachmentPreviewImageBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.previewmulti.listeners.MultipleAttachmentPreviewImageListener

class MultipleAttachmentPreviewImageFragment(
    val file: MultipleAttachmentFileItem
) : Fragment() {

    private lateinit var binding: FragmentMultipleAttachmentPreviewImageBinding
    private var isShowing = true
    private var listener: MultipleAttachmentPreviewImageListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultipleAttachmentPreviewImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.imagePreview.setOnClickListener {
            listener?.changeVisibilityOptions()
        }
        loadImage()
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

    fun setListener(listener: MultipleAttachmentPreviewImageListener) {
        this.listener = listener
    }

}