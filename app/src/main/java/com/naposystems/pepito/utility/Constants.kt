package com.naposystems.pepito.utility

object Constants {
    const val URL_TERMS_AND_CONDITIONS = "https://napoleonsecretchat.com/privacidad/"

    const val BASE_URL = "http://192.168.0.42:8889/api/"
    const val GENERATE_CODE = "auth/generatecode"
    const val VERIFICATE_CODE = "auth/verificatecode"
    const val VALIDATE_NICKNAME = "auth/validatenick"
    const val CREATE_ACCOUNT = "users"

    const val CODE_VALIDATED = 1
    const val ACCOUNT_CREATED = 2

    object SharedPreferences {
        const val PREF_NAME = "napoleon_preferences"
        const val PREF_LANGUAGE_SELECTED = "language_selected"
        const val PREF_ACCOUNT_STATUS = "account_status"
        const val PREF_FIREBASE_ID = "firebase_id"
    }

    object RegularExpressions {
        const val ONLY_LETTERS_AND_NUMBERS = "^[a-zA-Z0-9]*$"
        const val NICKNAME = "^[a-zA-Z0-9]{5,20}$"
        const val NAME_TO_SHOW = "^[a-zA-ZñÑ]+(([' ][a-zA-ZñÑ ])?[a-zA-Z]){5,50}$"
    }
}