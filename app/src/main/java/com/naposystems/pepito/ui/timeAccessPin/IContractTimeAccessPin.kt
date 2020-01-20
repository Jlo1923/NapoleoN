package com.naposystems.pepito.ui.timeAccessPin

interface IContractTimeAccessPin {

    interface ViewModel {
        fun getTimeAccessPin()
        fun setTimeAccessPin(time: Int)
    }

    interface Repository {
        suspend fun getTimeAccessPin(): Int
        suspend fun setTimeAccessPin(time: Int)
    }
}