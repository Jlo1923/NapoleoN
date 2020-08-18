package com.naposystems.napoleonchat.ui.previewBackgroundChat

interface IContractPreviewBackgroundChat {
    interface ViewModel {
        fun updateChatBackground(uri: String)
    }

    interface Repository {
        suspend fun updateChatBackground(newBackground: String)
    }
}