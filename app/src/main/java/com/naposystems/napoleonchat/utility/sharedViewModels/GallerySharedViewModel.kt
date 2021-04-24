package com.naposystems.napoleonchat.utility.sharedViewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class GallerySharedViewModel
@Inject constructor() : ViewModel() {

    private val _uriImageSelected = MutableLiveData<Uri>()
    val uriImageSelected: LiveData<Uri>
        get() = _uriImageSelected

    fun setImageUriSelected(uri: Uri) {
        this._uriImageSelected.value = uri
    }

    fun resetUriImageSelected() {
        this._uriImageSelected.value = null
    }

}