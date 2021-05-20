package com.naposystems.napoleonchat.repository.unlockAppTime

interface UnlockAppTimeRepository {
        suspend fun getUnlockTime(): Long
        suspend fun setAttempts(attempts: Int)
        suspend fun setLockType(type: Int)
}