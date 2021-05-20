package com.naposystems.napoleonchat.repository.splash

import com.naposystems.napoleonchat.source.local.entity.UserEntity

interface SplashRepository {
    suspend fun clearData()
    suspend fun getUser(): UserEntity
    suspend fun getTimeRequestAccessPin(): Int
    suspend fun getLockTime(): Long
    suspend fun getLockStatus(): Int
    suspend fun getLockType(): Int
    suspend fun getUnlockTimeApp(): Long
    suspend fun getAccountStatus(): Int
    suspend fun setDefaultLanguage(language: String)
    suspend fun setDefaultBiometricsOption(biometricOption: Int)
}

