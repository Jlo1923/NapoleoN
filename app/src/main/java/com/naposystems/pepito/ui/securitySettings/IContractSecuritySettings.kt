package com.naposystems.pepito.ui.securitySettings

interface IContractSecuritySettings {

    interface ViewModel {
        fun getSelfDestructTime()
        fun getAllowDownload()
        fun getMessageSelfDestructTimeNotSent()
        fun updateAllowDownload(state: Boolean)
        fun getBiometricsOption()
        fun getTimeRequestAccessPin()
    }

    interface Repository {
        fun getSelfDestructTime(): Int
        fun getAllowDownload(): Int
        fun updateAllowDownload(state: Int)
        fun getMessageSelfDestructTimeNotSent(): Int
        fun getBiometricsOption(): Int
        fun getTimeRequestAccessPin(): Int
    }
}