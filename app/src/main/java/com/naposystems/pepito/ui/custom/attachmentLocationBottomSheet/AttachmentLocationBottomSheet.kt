package com.naposystems.pepito.ui.custom.attachmentLocationBottomSheet

import android.content.Context
import android.location.Location
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.CustomViewAttachmentLocationBottomSheetBinding
import java.util.*

class AttachmentLocationBottomSheet constructor(context: Context, attributeSet: AttributeSet) :
    CoordinatorLayout(context, attributeSet),
    IContractAttachmentLocationBottomSheet {

    private var binding: CustomViewAttachmentLocationBottomSheetBinding
    private var bottomSheetBehavior: BottomSheetBehavior<View>
    private var mListener: AttachmentLocationBottomSheetListener? = null
    private var placeName: String = ""
    private var placeAddress: String = ""

    interface AttachmentLocationBottomSheetListener {
        fun onFabClicked()
    }

    init {
        val infService = Context.LAYOUT_INFLATER_SERVICE
        val layoutInflater =
            getContext().getSystemService(infService) as LayoutInflater

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.custom_view_attachment_location_bottom_sheet,
            this,
            true
        )

        binding.buttonSendLocation.setOnClickListener {
            mListener?.onFabClicked()
        }

        bottomSheetBehavior = BottomSheetBehavior.from(binding.rootBottomSheet)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    //region Implementation IContractAttachmentLocationBottomSheet

    override fun setListener(listener: AttachmentLocationBottomSheetListener) {
        this.mListener = listener
    }

    override fun showLoading() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.textViewPlaceName.text = ""
        binding.textViewPlaceAddress.text = ""
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun showResult(
        latitude: Double,
        longitude: Double,
        addressToShortString: String,
        addressToString: String
    ) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        binding.progressBar.visibility = View.GONE

        if (addressToString.isEmpty()) {
            val longString = Location.convert(longitude, Location.FORMAT_DEGREES)
            val latString = Location.convert(latitude, Location.FORMAT_DEGREES)

            placeName = String.format(Locale.getDefault(), "%s %s", latString, longString)

        } else {
            placeName = addressToShortString
            placeAddress = addressToString
        }


        binding.textViewPlaceName.text = placeName
        binding.textViewPlaceAddress.text = placeAddress
    }

    override fun hide() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun getPlaceName() = this.placeName

    override fun getPlaceAddress() = this.placeAddress

    //endregion
}