package com.naposystems.napoleonchat.dialog.activateBiometrics

interface ActivateBiometricsDialogRepository {
    suspend fun getBiometricsOption(): Int
    suspend fun setBiometricsOption(option: Int)
}