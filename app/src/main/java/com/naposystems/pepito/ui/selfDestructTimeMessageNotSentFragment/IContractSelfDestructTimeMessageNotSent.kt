package com.naposystems.pepito.ui.selfDestructTimeMessageNotSentFragment

interface IContractSelfDestructTimeMessageNotSent {
    interface ViewModel {
        fun getSelfDestructTimeMessageNotSent()
        fun setSelfDestructTimeMessageNotSent(time: Int)
    }

    interface Repository {
        suspend fun getSelfDestructTimeMessageNotSent(): Int
        suspend fun setSelfDestructTimeMessageNotSent(time: Int)
    }
}