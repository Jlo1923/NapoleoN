package com.naposystems.pepito.ui.userDisplayFormat

interface IContractUserDisplayFormat {

    interface ViewModel {
        fun setUserDisplayFormat(format: Int)
        fun getUserDisplayFormat()
    }

    interface Repository {
        fun setUserDisplayFormat(format: Int)
        fun getUserDisplayFormat(): Int
    }
}