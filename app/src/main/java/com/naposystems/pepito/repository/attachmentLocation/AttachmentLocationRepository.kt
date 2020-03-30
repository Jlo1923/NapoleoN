package com.naposystems.pepito.repository.attachmentLocation

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.naposystems.pepito.ui.attachmentLocation.IContractAttachmentLocation
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AttachmentLocationRepository @Inject constructor(private val context: Context) :
    IContractAttachmentLocation.Repository {

    override suspend fun getAddress(latLng: LatLng): Address? {
        return withContext(IO) {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val result: List<Address> =
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (result.isNotEmpty()) result[0] else null
            } catch (exception: Exception) {
                Timber.e(exception)
                null
            }
        }
    }
}