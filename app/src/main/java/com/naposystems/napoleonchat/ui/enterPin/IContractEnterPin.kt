package com.naposystems.napoleonchat.ui.enterPin

import com.naposystems.napoleonchat.entity.User

interface IContractEnterPin {
    interface ViewModel {
        fun validatedAccessPin(pin: String)
        fun getAttempts()
        fun setAttempts(attempts: Int)
        fun setTotalAttempts(attempts: Int)
        fun setLockStatus(state: Int)
        fun getBiometricsOption()
        fun setBiometricPreference(option: Int)
    }
    interface Repository {
        suspend fun getAccessPin(): User
        suspend fun getAttempts(): Int
        suspend fun getTotalAttempts(): Int
        suspend fun setAttempts(attempts: Int)
        suspend fun setTotalAttempts(attempts: Int)
        suspend fun setUnlockAppTime(time: Long)
        suspend fun setLockType(type: Int)
        suspend fun setLockStatus(state: Int)
        suspend fun getBiometricsOption(): Int
        fun setBiometricPreference(option: Int)
    }
}