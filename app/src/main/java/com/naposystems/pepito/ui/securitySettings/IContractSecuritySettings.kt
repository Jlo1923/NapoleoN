package com.naposystems.pepito.ui.securitySettings

interface IContractSecuritySettings {

    interface ViewModel {
        fun getSelfDestructTime()
    }

    interface Repository {
        fun getSelfDestructTime(): Int
    }
}