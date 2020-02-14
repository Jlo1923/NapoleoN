package com.naposystems.pepito.ui.conversationCamera

interface IContractConversationCamera {

    interface ViewModel {
        fun setSendClicked()
        fun setMessage(message: String)
        fun resetMessage()
        fun resetSendClicked()
        fun setBase64(base64: String)
        fun getBase64(): String
        fun setUri(uri: String)
        fun getUri(): String
    }
}