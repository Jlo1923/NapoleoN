package com.naposystems.pepito.utility.sharedViewModels.camera

import android.net.Uri

interface IContractCameraShare {

    interface ViewModel {
        fun setImageUriTaken(uri: Uri)
        fun resetUriImageTaken()
    }

}