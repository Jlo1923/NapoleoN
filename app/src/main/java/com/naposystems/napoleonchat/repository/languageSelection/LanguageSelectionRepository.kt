package com.naposystems.napoleonchat.repository.languageSelection

import android.content.Context
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.dto.language.UserLanguageReqDTO
import com.naposystems.napoleonchat.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.model.languageSelection.Language
import com.naposystems.napoleonchat.ui.languageSelection.IContractLanguageSelection
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import retrofit2.Response
import javax.inject.Inject

class LanguageSelectionRepository @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
) :
    IContractLanguageSelection.Repository {

    override fun getLanguages(): List<Language> {
        val languages = context.resources
            .openRawResource(R.raw.languages)
            .bufferedReader()
            .use { it.readLine() }

        val moshi = Moshi.Builder().build()

        val listType = Types.newParameterizedType(List::class.java, Language::class.java)
        val adapter: JsonAdapter<List<Language>> = moshi.adapter(listType)

        return adapter.fromJson(languages)!!
    }

    override suspend fun updateUserLanguage(language: Language): Response<UpdateUserInfoResDTO> {
        return napoleonApi.updateUserLanguage(UserLanguageReqDTO(languageIso = language.iso))
    }

    override suspend fun updateUserLanguagePreference(languageIso: String) {
        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_LANGUAGE_SELECTED,
            languageIso
        )
    }
}