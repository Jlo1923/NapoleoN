package com.naposystems.pepito.ui.baseFragment

interface IContractBase {

    interface ViewModel {
        fun outputControl(state: Int)
    }
    
    interface Repository {
        suspend fun outputControl(state: Int)
    }
}