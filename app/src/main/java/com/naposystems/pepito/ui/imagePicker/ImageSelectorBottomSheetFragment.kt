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
import com.naposystems.pepito.utility.Constants

const val TITLE = "TITLE"
const val LOCATION = "LOCATION"

class ImageSelectorBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(title: String, location : Int) =
            ImageSelectorBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                    putInt(LOCATION, location)
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
    private lateinit var binding: ImageSelectorBottomSheetFragmentBinding
    private lateinit var viewModel: ImageSelectorBottomSheetViewModel
    private lateinit var listener: OnOptionSelected

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {bundle ->
            title = bundle.getString(TITLE)
            location = bundle.getInt(LOCATION)
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
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation
        viewModel = ViewModelProviders.of(this).get(ImageSelectorBottomSheetViewModel::class.java)

        when(location) {
            Constants.LocationImageSelectorBottomSheet.CONTACT_PROFILE.location -> {
                binding.containerDefault.visibility = View.GONE
            }
            else -> {
                binding.containerDefault.visibility = View.VISIBLE
            }
        }
    }

    fun setListener(listener: OnOptionSelected) {
        this.listener = listener
    }

}
