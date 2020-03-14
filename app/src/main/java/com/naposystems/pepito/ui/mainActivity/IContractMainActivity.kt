package com.naposystems.pepito.ui.mainActivity

import com.naposystems.pepito.entity.User

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
    }
}