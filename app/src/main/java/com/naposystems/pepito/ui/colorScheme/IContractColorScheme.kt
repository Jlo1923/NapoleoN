package com.naposystems.pepito.ui.colorScheme

interface IContractColorScheme {

    interface ViewModel {
        fun getActualTheme()
        fun setTheme(theme: Int)
        fun saveTheme(newTheme: Int)
    }

    interface Repository {
        fun getActualTheme(): Int
        suspend fun saveTheme(newTheme: Int)
    }
}