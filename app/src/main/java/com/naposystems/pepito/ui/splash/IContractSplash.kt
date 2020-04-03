package com.naposystems.pepito.ui.splash

import com.naposystems.pepito.entity.User

interface IContractSplash {
    interface ViewModel {
        fun getUser()
        fun getTimeRequestAccessPin()
        fun getLockTime()
        fun getLockStatus()
        fun getLockType()
        fun getUnlockTimeApp()
        fun getAccountStatus(): Int
        fun setDefaultPreferences()
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
        suspend fun setDefaultTheme()
        suspend fun setDefaultLanguage(language: String)
        suspend fun setDefaultUserDisplayFormat()
        suspend fun setDefaultSelfDestructTime()
        suspend fun setDefaultTimeRequestAccessPin()
        suspend fun setDefaultAllowDownloadAttachments()
        suspend fun setDefaultBiometricsOption(biometricOption: Int)
        suspend fun setDefaultLockType()
        suspend fun setDefaultSelfDestructTimeMessageNotSent()
        suspend fun setDefaultAttemptsForRetryCode()
        suspend fun setDefaultTimeForRetryCode()
        suspend fun setDefaultAttemptsForNewCode()
    }
}

