package com.naposystems.napoleonchat.repository.appearanceSettings

interface AppearanceSettingsRepository {
    fun getColorScheme(): Int
    fun getUserDisplayFormat(): Int
    fun getTimeFormat(): Int
    suspend fun getConversationBackground(): String
}