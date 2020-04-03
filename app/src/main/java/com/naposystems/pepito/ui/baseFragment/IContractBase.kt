package com.naposystems.pepito.ui.baseFragment

interface IContractBase {

    interface ViewModel {
        fun outputControl(state: Int)
        fun getOutputControl()
    }
    
    interface Repository {
        suspend fun outputControl(state: Int)
        suspend fun getOutputControl(): Int
    }
}