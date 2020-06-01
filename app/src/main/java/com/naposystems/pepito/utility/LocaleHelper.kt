package com.naposystems.pepito.utility

import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.naposystems.pepito.R
import com.naposystems.pepito.model.languageSelection.Language
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import timber.log.Timber
import java.util.*

class LocaleHelper(base: Context) : ContextWrapper(base) {

    companion object {

        fun setLocale(context: Context?): ContextWrapper {
            return updateResources(context, getLanguagePreference(context!!))
        }

        fun setNewLanguage(context: Context, languageSelected: Language): ContextWrapper {
            setLanguagePreference(context, languageSelected)
            return updateResources(context, languageSelected.iso)
        }

        private fun updateResources(context: Context?, iso: String): ContextWrapper {
            Timber.d("el puto iso de mierda: $iso")
            val newContext: Context?

            val res = context!!.resources
            val config = res.configuration

            val locale = Locale(iso)
            Locale.setDefault(locale)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setSystemLocale(config, locale)
            } else {
                setSystemLocaleLegacy(config, locale)
            }

            newContext = context.createConfigurationContext(config)
            return ContextWrapper((newContext))
        }

        @SuppressWarnings("deprecation")
        fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        @TargetApi(Build.VERSION_CODES.N)
        fun setSystemLocale(config: Configuration, locale: Locale) {
            config.setLocale(locale)
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
            val sharedPreferences = SharedPreferencesManager(context)
            sharedPreferences.putString(
                Constants.SharedPreferences.PREF_LANGUAGE_SELECTED,
                language.iso
            )
        }

        fun getLanguagePreference(context: Context): String {
            val sharedPreferences = SharedPreferencesManager(context)

            val prefLanguage = sharedPreferences.getString(
                Constants.SharedPreferences.PREF_LANGUAGE_SELECTED,
                ""
            )

            return if (prefLanguage.isEmpty()) {
                var defaultLanguage = Locale.getDefault().language

                val filterLanguage = getLanguages(
                    context
                ).filter {
                    it.iso == defaultLanguage
                }

                if (filterLanguage.isEmpty()) {
                    defaultLanguage = "en"
                }

                defaultLanguage
            } else {
                prefLanguage
            }
        }
    }
}