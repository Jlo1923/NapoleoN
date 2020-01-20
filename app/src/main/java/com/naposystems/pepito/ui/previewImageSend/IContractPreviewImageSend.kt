package com.naposystems.pepito.ui.previewImageSend

interface IContractPreviewImageSend {

    interface ViewModel {
        fun setCancelClicked()
        fun setMessage(message: String)
        fun resetMessage()
        fun resetCancelClicked()
    }
}