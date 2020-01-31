package com.naposystems.pepito.ui.unlockAppTime

interface IContractUnlockAppTime {
    interface ViewModel {
        fun getUnlockTime()
        fun setAttempts(attempts: Int)
        fun setLockType(type: Int)
    }
    interface Repository {
        suspend fun getUnlockTime(): Long
        suspend fun setAttempts(attempts: Int)
        suspend fun setLockType(type: Int)
    }
}