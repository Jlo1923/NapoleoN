package com.naposystems.napoleonchat.ui.appearanceSettings

interface IContractAppearanceSettings {

    interface ViewModel {
        fun getColorScheme()
        fun getUserDisplayFormat()
        fun getTimeFormat()
        fun getConversationBackground()
        fun resetConversationBackgroundLiveData()
    }

    interface Repository {
        fun getColorScheme(): Int
        fun getUserDisplayFormat(): Int
        fun getTimeFormat(): Int
        suspend fun getConversationBackground(): String
    }
}