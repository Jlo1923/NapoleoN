package com.naposystems.pepito.ui.timeAccessPin

interface IContractTimeAccessPin {

    interface ViewModel {
        fun getTimeAccessPin()
        fun setTimeAccessPin(time: Int)
        fun setLockType(type: Int)
    }

    interface Repository {
        suspend fun getTimeAccessPin(): Int
        suspend fun setTimeAccessPin(time: Int)
        suspend fun setLockType(type: Int)
    }
}