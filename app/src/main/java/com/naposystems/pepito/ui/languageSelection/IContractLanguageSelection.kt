package com.naposystems.pepito.ui.languageSelection

import com.naposystems.pepito.dto.profile.UpdateUserInfoResDTO
import com.naposystems.pepito.model.languageSelection.Language
import retrofit2.Response

interface IContractLanguageSelection {

    interface ViewModel {
        fun getLanguages(): List<Language>
        fun resetErrorUpdatingLanguage()
    }

    interface Repository {
        fun getLanguages(): List<Language>
        suspend fun updateUserLanguage(language: Language): Response<UpdateUserInfoResDTO>
        suspend fun updateUserLanguagePreference(languageIso: String)
    }
}