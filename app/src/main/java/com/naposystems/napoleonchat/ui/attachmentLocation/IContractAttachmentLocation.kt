package com.naposystems.napoleonchat.ui.attachmentLocation

import android.location.Address
import com.google.android.gms.maps.model.LatLng

interface IContractAttachmentLocation {

    interface ViewModel {
        fun getAddress(latLng: LatLng)
    }

    interface Repository {
        suspend fun getAddress(latLng: LatLng): Address?
    }
}