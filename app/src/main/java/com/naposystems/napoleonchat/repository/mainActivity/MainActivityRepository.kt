package com.naposystems.napoleonchat.repository.mainActivity

import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity

interface MainActivityRepository {
    suspend fun getUser(): UserEntity
    suspend fun getAccountStatus(): Int
    fun getOutputControl(): Int
    suspend fun setOutputControl(state: Int)
    fun getTimeRequestAccessPin(): Int
    suspend fun getLockTimeApp(): Long
    suspend fun setLockStatus(state: Int)
    fun setLockTimeApp(lockTime: Long)
    fun setJsonNotification(json: String)
    suspend fun getContactById(contactId: Int): ContactEntity?
    fun getRecoveryQuestionsPref(): Int
    fun disconnectSocket()
}