package com.naposystems.pepito.repository.languageSelection

import android.content.Context
import com.naposystems.pepito.R
import com.naposystems.pepito.model.languageSelection.Language
import com.naposystems.pepito.ui.languageSelection.IContractLanguageSelection
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject

class LanguageSelectionRepository @Inject constructor(private val context: Context) :
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
}