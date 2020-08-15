package com.naposystems.napoleonchat.ui.securitySettings

interface IContractSecuritySettings {

    interface ViewModel {
        fun getAllowDownload()
        fun updateAllowDownload(state: Boolean)
        fun getBiometricsOption()
        fun getTimeRequestAccessPin()
    }

    interface Repository {
        fun getAllowDownload(): Int
        fun updateAllowDownload(state: Int)
        fun getBiometricsOption(): Int
        fun getTimeRequestAccessPin(): Int
    }
}