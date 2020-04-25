package com.naposystems.pepito.ui.appearanceSettings

interface IContractAppearanceSettings {

    interface ViewModel {
        fun getColorScheme()
        fun getUserDisplayFormat()
        fun getTimeFormat()
    }

    interface Repository {
        fun getColorScheme(): Int
        fun getUserDisplayFormat(): Int
        fun getTimeFormat(): Int
    }
}