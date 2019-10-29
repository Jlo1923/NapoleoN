package com.naposystems.pepito.utility

import android.content.Context
import android.content.ContextWrapper
import com.naposystems.pepito.model.languageSelection.Language
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

        private fun setLanguagePreference(context: Context, language: Language) {
            val sharedPreferences =
                context.getSharedPreferences(SharedPreference.PREF_NAME, Context.MODE_PRIVATE)

            with(sharedPreferences!!.edit()) {
                putString(
                    SharedPreference.PREF_LANGUAGE_SELECTED,
                    language.iso
                )
                commit()
            }
        }

        fun getLanguagePreference(context: Context): String {
            val sharedPreferences =
                context.getSharedPreferences(SharedPreference.PREF_NAME, Context.MODE_PRIVATE)

            val defaultLanguage = "en"

            return sharedPreferences.getString(
                SharedPreference.PREF_LANGUAGE_SELECTED,
                defaultLanguage
            )!!
        }
    }
}