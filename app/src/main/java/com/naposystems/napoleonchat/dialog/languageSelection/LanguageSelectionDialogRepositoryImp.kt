package com.naposystems.napoleonchat.dialog.languageSelection

import android.content.Context
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.model.languageSelection.Language
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.language.UserLanguageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.profile.UpdateUserInfoResDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import retrofit2.Response
import javax.inject.Inject

class LanguageSelectionDialogRepositoryImp
@Inject constructor(
    private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
) : LanguageSelectionDialogRepository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override fun getLanguages(): List<Language> {
        val languages = context.resources
            .openRawResource(R.raw.languages)
            .bufferedReader()
            .use { it.readLine() }

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