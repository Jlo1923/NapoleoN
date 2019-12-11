package com.naposystems.pepito.ui.securitySettings

interface IContractSecuritySettings {

    interface ViewModel {
        fun getSelfDestructTime()
        fun getTimeRequestAccessPin()
    }

    interface Repository {
        fun getSelfDestructTime(): Int
        fun getTimeRequestAccessPin(): Int
    }
}