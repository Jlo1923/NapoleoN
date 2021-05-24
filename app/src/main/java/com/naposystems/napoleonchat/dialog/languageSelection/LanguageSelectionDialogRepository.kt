package com.naposystems.napoleonchat.dialog.languageSelection

import com.naposystems.napoleonchat.model.languageSelection.Language
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoResDTO
import retrofit2.Response

interface LanguageSelectionDialogRepository {
    fun getLanguages(): List<Language>
    suspend fun updateUserLanguage(language: Language): Response<UpdateUserInfoResDTO>
    suspend fun updateUserLanguagePreference(languageIso: String)
}