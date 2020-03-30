package com.naposystems.pepito.ui.custom.attachmentLocationBottomSheet

interface IContractAttachmentLocationBottomSheet {

    fun setListener(listener: AttachmentLocationBottomSheet.AttachmentLocationBottomSheetListener)

    fun showLoading()

    fun showResult(
        latitude: Double,
        longitude: Double,
        addressToShortString: String,
        addressToString: String
    )

    fun hide()

    fun getPlaceName() : String

    fun getPlaceAddress(): String
}