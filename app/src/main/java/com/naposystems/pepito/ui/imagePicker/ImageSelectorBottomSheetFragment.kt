package com.naposystems.pepito.ui.imagePicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ImageSelectorBottomSheetFragmentBinding

const val TITLE = "TITLE"

class ImageSelectorBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(title: String) =
            ImageSelectorBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                }
            }
    }

    interface OnOptionSelected {
        fun takeImageOptionSelected()
        fun galleryOptionSelected()
    }

    private var title: String? = null
    private lateinit var binding: ImageSelectorBottomSheetFragmentBinding
    private lateinit var viewModel: ImageSelectorBottomSheetViewModel
    private lateinit var listener: OnOptionSelected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.image_selector_bottom_sheet_fragment,
            container,
            false
        )

        binding.textViewTitle.text = title

        binding.containerCameraOption.setOnClickListener {
            listener.takeImageOptionSelected()
            dismiss()
        }

        binding.containerGalleryOption.setOnClickListener {
            listener.galleryOptionSelected()
            dismiss()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation
        viewModel = ViewModelProviders.of(this).get(ImageSelectorBottomSheetViewModel::class.java)
    }

    fun setListener(listener: OnOptionSelected) {
        this.listener = listener
    }

}
