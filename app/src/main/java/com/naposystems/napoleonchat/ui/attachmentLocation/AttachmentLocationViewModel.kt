package com.naposystems.napoleonchat.ui.attachmentLocation

import android.location.Address
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttachmentLocationViewModel @Inject constructor(
    private val repository: IContractAttachmentLocation.Repository
) : ViewModel(), IContractAttachmentLocation.ViewModel {

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    override fun getAddress(latLng: LatLng) {
        viewModelScope.launch {
            _address.value = repository.getAddress(latLng)
        }
    }
}