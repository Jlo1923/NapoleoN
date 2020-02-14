package com.naposystems.pepito.utility

import android.content.Context
import android.content.ContextWrapper
import com.naposystems.pepito.R
import com.naposystems.pepito.model.languageSelection.Language
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.*

class LocaleHelper(base: Context?) : ContextWrapper(base) {

    companion object {

        fun setLocale(context: Context?): Context {
            return updateResources(context, getLanguagePreference(context!!))
        }

        fun setNewLanguage(context: Context, languageSelected: Language): Context {
            setLanguagePreference(context, languageSelected)
            return updateResources(context, languageSelected.iso)
        }

        private fun updateResources(context: Context?, iso: String): Context {
            val newContext: Context

            val locale = Locale(iso)
            Locale.setDefault(locale)

            val res = context!!.resources
            val config = res.configuration

            config.setLocale(locale)
            newContext = context.createConfigurationContext(config)
            return newContext
        }

        private fun getLanguages(context: Context): List<Language> {
            val languages = context.resources
                .openRawResource(R.raw.languages)
                .bufferedReader()
                .use { it.readLine() }

            val moshi = Moshi.Builder().build()

            val listType = Types.newParameterizedType(List::class.java, Language::class.java)
            val adapter: JsonAdapter<List<Language>> = moshi.adapter(listType)

            return adapter.fromJson(languages)!!
        }

        private fun setLanguagePreference(context: Context, language: Language) {
            val sharedPreferences =
                context.getSharedPreferences(
                    Constants.SharedPreferences.PREF_NAME,
                    Context.MODE_PRIVATE
                )

            with(sharedPreferences!!.edit()) {
                putString(
                    Constants.SharedPreferences.PREF_LANGUAGE_SELECTED,
                    language.iso
                )
                commit()
            }
        }

        fun getLanguagePreference(context: Context): String {
            val sharedPreferences =
                context.getSharedPreferences(
                    Constants.SharedPreferences.PREF_NAME,
                    Context.MODE_PRIVATE
                )

            var defaultLanguage = Locale.getDefault().language

            val filterLanguage = getLanguages(
                context
            ).filter {
                it.iso == defaultLanguage
            }

            if (filterLanguage.isEmpty()) {
                defaultLanguage = "en"
            }

            return sharedPreferences.getString(
                Constants.SharedPreferences.PREF_LANGUAGE_SELECTED,
                defaultLanguage
            )!!
        }
    }
}