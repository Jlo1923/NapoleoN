package com.naposystems.napoleonchat.utility.sharedViewModels.gallery

import android.net.Uri

interface IContractGalleryShare {

    interface ViewModel {
        fun setImageUriSelected(uri: Uri)
        fun resetUriImageSelected()
    }

}