package com.naposystems.pepito.ui.colorScheme

interface IContractColorScheme {

    interface ViewModel {
        fun getActualTheme()
        fun setTheme(newTheme: Int)
    }

    interface Repository {
        fun getActualTheme(): Int
        suspend fun setTheme(newTheme: Int)
    }
}