package com.naposystems.pepito.ui.appearanceSettings

interface IContractAppearanceSettings {

    interface ViewModel {
        fun getColorScheme()
        fun getUserDisplayFormat()
    }

    interface Repository {
        fun getColorScheme(): Int
        fun getUserDisplayFormat(): Int
    }
}