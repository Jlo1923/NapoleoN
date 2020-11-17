package com.naposystems.napoleonchat.ui.splash

import com.naposystems.napoleonchat.entity.User

interface IContractSplash {
    interface ViewModel {
        fun getUser()
        fun getTimeRequestAccessPin()
        fun getLockTime()
        fun getLockStatus()
        fun getLockType()
        fun getUnlockTimeApp()
        fun getAccountStatus()
        fun setDefaultLanguage(language: String)
        fun setDefaultBiometricsOption(biometricOption: Int)
    }

    interface Repository {
        suspend fun getUser(): User
        suspend fun getTimeRequestAccessPin(): Int
        suspend fun getLockTime(): Long
        suspend fun getLockStatus(): Int
        suspend fun getLockType(): Int
        suspend fun getUnlockTimeApp(): Long
        suspend fun getAccountStatus(): Int
        suspend fun setDefaultLanguage(language: String)
        suspend fun setDefaultBiometricsOption(biometricOption: Int)
    }
}

