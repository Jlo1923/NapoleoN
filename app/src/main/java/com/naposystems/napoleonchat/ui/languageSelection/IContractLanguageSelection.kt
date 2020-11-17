package com.naposystems.napoleonchat.ui.languageSelection

import com.naposystems.napoleonchat.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.model.languageSelection.Language
import retrofit2.Response

interface IContractLanguageSelection {

    interface ViewModel {
        fun setSelectedLanguage(language: Language, location : Int)
        fun getLanguages(): List<Language>
        suspend fun updateLanguageLocal(language: Language)
        fun resetErrorUpdatingLanguage()
    }

    interface Repository {
        fun getLanguages(): List<Language>
        suspend fun updateUserLanguage(language: Language): Response<UpdateUserInfoResDTO>
        suspend fun updateUserLanguagePreference(languageIso: String)
    }
}