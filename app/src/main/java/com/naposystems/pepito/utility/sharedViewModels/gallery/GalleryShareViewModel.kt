package com.naposystems.pepito.utility.sharedViewModels.gallery

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class GalleryShareViewModel @Inject constructor() : ViewModel(), IContractGalleryShare.ViewModel {

    private val _uriImageSelected = MutableLiveData<Uri>()
    val uriImageSelected: LiveData<Uri>
        get() = _uriImageSelected

    override fun setImageUriSelected(uri: Uri) {
        this._uriImageSelected.value = uri
    }

    override fun resetUriImageSelected() {
        this._uriImageSelected.value = null
    }

}