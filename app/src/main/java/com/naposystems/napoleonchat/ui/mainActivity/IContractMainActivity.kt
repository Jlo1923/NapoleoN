package com.naposystems.napoleonchat.ui.mainActivity

import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.User

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
        fun resetIsOnCallPref()
        fun getRecoveryQuestionsPref(): Int
    }

    interface Repository {
        suspend fun getUser(): User
        suspend fun getAccountStatus(): Int
        fun getOutputControl(): Int
        suspend fun setOutputControl(state: Int)
        fun getTimeRequestAccessPin(): Int
        suspend fun getLockTimeApp(): Long
        suspend fun setLockStatus(state: Int)
        fun setLockTimeApp(lockTime: Long)
        fun setJsonNotification(json: String)
        suspend fun getContactById(contactId: Int): Contact?
        fun resetIsOnCallPref()
        fun getRecoveryQuestionsPref(): Int
    }
}