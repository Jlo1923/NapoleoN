package com.naposystems.napoleonchat.repository.securitySettings

interface SecuritySettingsRepository {
    fun getAllowDownload(): Int
    fun updateAllowDownload(state: Int)
    fun getBiometricsOption(): Int
    fun getTimeRequestAccessPin(): Int
}