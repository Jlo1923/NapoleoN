package com.naposystems.napoleonchat.ui.baseFragment

interface IContractBase {

    interface ViewModel {
        fun outputControl(state: Int)
        fun getOutputControl()
        fun connectSocket()
    }
    
    interface Repository {
        suspend fun outputControl(state: Int)
        suspend fun getOutputControl(): Int
        fun connectSocket()
    }
}