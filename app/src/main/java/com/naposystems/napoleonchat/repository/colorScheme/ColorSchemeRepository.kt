package com.naposystems.napoleonchat.repository.colorScheme

interface ColorSchemeRepository {
    fun getActualTheme(): Int
    suspend fun saveTheme(newTheme: Int)
}