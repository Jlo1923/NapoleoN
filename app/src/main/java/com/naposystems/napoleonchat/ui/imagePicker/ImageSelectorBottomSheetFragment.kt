package com.naposystems.napoleonchat.ui.imagePicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ImageSelectorBottomSheetFragmentBinding

const val TITLE = "TITLE"
const val LOCATION = "LOCATION"
const val SHOW_DEFAULT = "SHOW_DEFAULT"

class ImageSelectorBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(title: String, location: Int, showDefault: Boolean) =
            ImageSelectorBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                    putInt(LOCATION, location)
                    putBoolean(SHOW_DEFAULT, showDefault)
                }
            }
    }

    interface OnOptionSelected {
        fun takeImageOptionSelected(location: Int)
        fun galleryOptionSelected(location: Int)
        fun defaultOptionSelected(location: Int)
    }

    private var title: String? = null
    private var location: Int? = null
    private var showDefault: Boolean = false
    private lateinit var binding: ImageSelectorBottomSheetFragmentBinding
    private val viewModel: ImageSelectorBottomSheetViewModel by viewModels()
    private lateinit var listener: OnOptionSelected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            title = bundle.getString(TITLE)
            location = bundle.getInt(LOCATION)
            showDefault = bundle.getBoolean(SHOW_DEFAULT)
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

        binding.containerDefault.visibility = if (showDefault) View.VISIBLE else View.GONE

        binding.containerCameraOption.setOnClickListener {
            location?.let { location ->
                listener.takeImageOptionSelected(location)
                dismiss()
            }
        }

        binding.containerGalleryOption.setOnClickListener {
            location?.let { location ->
                listener.galleryOptionSelected(location)
                dismiss()
            }
        }

        binding.containerDefault.setOnClickListener {
            location?.let { location ->
                listener.defaultOptionSelected(location)
                dismiss()
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    fun setListener(listener: OnOptionSelected) {
        this.listener = listener
    }

}
