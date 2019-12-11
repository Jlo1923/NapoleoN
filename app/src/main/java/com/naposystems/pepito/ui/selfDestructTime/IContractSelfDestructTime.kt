package com.naposystems.pepito.ui.selfDestructTime

interface IContractSelfDestructTime {

    interface ViewModel {
        fun getSelfDestructTime()
        fun setSelfDestructTime(selfDestructTime: Int)
    }

    interface Repository {
        fun getSelfDestructTime(): Int
        fun setSelfDestructTime(selfDestructTime: Int)
    }
}