package com.naposystems.pepito.ui.activateBiometrics

interface IContractActivateBiometrics {
    interface ViewModel {
        fun getBiometricsOption()
        fun setBiometricsOption(option: Int)
    }

    interface Repository {
        suspend fun getBiometricsOption(): Int
        suspend fun setBiometricsOption(option: Int)
    }
}