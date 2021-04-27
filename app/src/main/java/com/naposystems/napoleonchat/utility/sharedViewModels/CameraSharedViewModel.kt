package com.naposystems.napoleonchat.utility.sharedViewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class CameraSharedViewModel
@Inject constructor() : ViewModel() {

    private val _uriImageTaken = MutableLiveData<Uri>()
    val uriImageTaken: LiveData<Uri>
        get() = _uriImageTaken

    fun setImageUriTaken(uri: Uri) {
        this._uriImageTaken.value = uri
    }

    fun resetUriImageTaken() {
        this._uriImageTaken.value = null
    }

}