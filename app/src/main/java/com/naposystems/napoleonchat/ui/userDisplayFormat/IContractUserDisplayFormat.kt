package com.naposystems.napoleonchat.ui.userDisplayFormat

interface IContractUserDisplayFormat {

    interface ViewModel {
        fun setUserDisplayFormat(format: Int)
        fun getUserDisplayFormat() : Int
    }

    interface Repository {
        fun setUserDisplayFormat(format: Int)
        fun getUserDisplayFormat(): Int
    }
}