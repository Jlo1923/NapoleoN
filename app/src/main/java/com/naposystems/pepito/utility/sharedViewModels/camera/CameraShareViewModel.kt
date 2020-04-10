package com.naposystems.pepito.utility.sharedViewModels.camera

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class CameraShareViewModel @Inject constructor()  : ViewModel(), IContractCameraShare.ViewModel {

    private val _uriImageTaken = MutableLiveData<Uri>()
    val uriImageTaken: LiveData<Uri>
        get() = _uriImageTaken

    override fun setImageUriTaken(uri: Uri) {
        this._uriImageTaken.value = uri
    }

    override fun resetUriImageTaken() {
        this._uriImageTaken.value = null
    }

}