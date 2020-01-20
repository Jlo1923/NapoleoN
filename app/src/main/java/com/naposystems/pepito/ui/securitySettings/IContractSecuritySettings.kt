package com.naposystems.pepito.ui.securitySettings

interface IContractSecuritySettings {

    interface ViewModel {
        fun getSelfDestructTime()
        fun getTimeRequestAccessPin()
        fun getAllowDownload()
        fun updateAllowDownload(state: Boolean)
    }

    interface Repository {
        fun getSelfDestructTime(): Int
        fun getTimeRequestAccessPin(): Int
        fun getAllowDownload(): Int
        fun updateAllowDownload(state: Int)
    }
}