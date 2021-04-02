package com.naposystems.napoleonchat.ui.mainActivity

import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity

interface IContractMainActivity {

    interface ViewModel {
        fun getUser()
        fun getAccountStatus()
        fun getOutputControl(): Int
        fun setOutputControl(state: Int)
        fun setLockTimeApp()
        fun getLockTimeApp(): Long
        fun getTimeRequestAccessPin(): Int
        fun setLockStatus(state: Int)
        fun setJsonNotification(json: String)
        fun getContact(contactId: Int)
        fun resetContact()
        fun setCallChannel(channel: String)
        fun getCallChannel(): String
        fun resetCallChannel()
        fun setIsVideoCall(isVideoCall: Boolean)
        fun isVideoCall(): Boolean?
        fun resetIsVideoCall()
        fun getRecoveryQuestionsPref(): Int
        fun disconnectSocket()
    }

    interface Repository {
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
}