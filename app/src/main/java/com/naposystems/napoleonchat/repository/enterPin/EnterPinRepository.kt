package com.naposystems.napoleonchat.repository.enterPin

import com.naposystems.napoleonchat.source.local.entity.UserEntity

interface EnterPinRepository {
    suspend fun getAccessPin(): UserEntity
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