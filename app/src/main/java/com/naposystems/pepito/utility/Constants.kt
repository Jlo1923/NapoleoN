package com.naposystems.pepito.utility

object Constants {
    const val URL_TERMS_AND_CONDITIONS = "https://napoleonsecretchat.com/privacidad/"
    const val URL_FREQUENT_QUESTIONS = "https://napoleonsecretchat.com/privacidad/"

    object NapoleonApi {
        const val BASE_URL =
            "http://nn-backend-secret-chatlb-1192195645.us-west-2.elb.amazonaws.com/api/"
        const val GENERATE_CODE = "auth/generatecode"
        const val VERIFICATE_CODE = "auth/verificatecode"
        const val VALIDATE_NICKNAME = "auth/validatenick"
        const val CREATE_ACCOUNT = "users"
        const val UPDATE_USER_INFO = "users/updateinfo"
        const val GET_BLOCKED_CONTACTS = "frienship/search/block"
    }

    enum class AccountStatus constructor(val id: Int) {
        CODE_VALIDATED(1),
        ACCOUNT_CREATED(2)
    }

    enum class ColorScheme constructor(val scheme: Int) {
        LIGHT_THEME(1),
        DARK_THEME(2)
    }

    enum class UserDisplayFormat constructor(val format: Int) {
        NAME_AND_NICKNAME(1),
        ONLY_NAME(2),
        ONLY_NICKNAME(3)
    }

    enum class SelfDestructTime constructor(val time: Int) {
        EVERY_FIVE_MINUTES(1),
        EVERY_FIFTEEN_MINUTES(2),
        EVERY_THIRTY_MINUTES(3),
        EVERY_ONE_HOUR(4),
        EVERY_SIX_HOURS(5),
        EVERY_TWENTY_FOUR_HOURS(6)
    }

    enum class TimeRequestAccessPin constructor(val time: Int) {
        THIRTY_SECONDS(1),
        ONE_MINUTE(2),
        FIVE_MINUTES(3),
        FIFTEEN_MINUTES(4),
        NEVER(5)
    }

    object SharedPreferences {
        const val PREF_NAME = "napoleon_preferences"
        const val PREF_LANGUAGE_SELECTED = "language_selected"
        const val PREF_ACCOUNT_STATUS = "account_status"
        const val PREF_FIREBASE_ID = "firebase_id"
        const val PREF_COLOR_SCHEME = "color_scheme"
        const val PREF_USER_DISPLAY_FORMAT = "user_display_format"
        const val PREF_SELF_DESTRUCT_TIME = "self_destruct_time"
        const val PREF_TIME_REQUEST_ACCESS_PIN = "time_request_access_pin"
    }

    object RegularExpressions {
        const val ONLY_LETTERS_AND_NUMBERS = "^[a-zA-Z0-9]*$"
        const val NICKNAME = "^[a-zA-Z0-9]{5,20}$"
        const val NAME_TO_SHOW = "^[a-zA-ZñÑ]+(([' ][a-zA-ZñÑ ])?[a-zA-Z]){5,50}$"
    }
}