package com.naposystems.pepito.ui.appearanceSettings

interface IContractAppearanceSettings {

    interface ViewModel {
        fun getColorScheme()
        fun getUserDisplayFormat()
        fun updateChatBackground(uri: String)
    }

    interface Repository {
        fun getColorScheme(): Int
        fun getUserDisplayFormat(): Int
        suspend fun updateChatBackground(newBackground: String)
    }
}