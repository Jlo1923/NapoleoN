package com.naposystems.pepito.utility

object Constants {
    const val URL_TERMS_AND_CONDITIONS = "https://napoleonsecretchat.com/privacidad/"
    const val URL_FREQUENT_QUESTIONS = "https://napoleonsecretchat.com/privacidad/"

    object NapoleonApi {
        const val BASE_URL = "http://192.168.1.222/nn-backend-secret-chat/public/api/"
        const val SOCKET_BASE_URL = "http://192.168.1.222:6001"
        const val GENERATE_CODE = "auth/sendverificationcode"
        const val VERIFICATE_CODE = "auth/validateverificationcode"
        const val VALIDATE_NICKNAME = "auth/validatenick"
        const val CREATE_ACCOUNT = "users"
        const val UPDATE_USER_INFO = "users/updateinfo"
        const val SEND_PQRS = "pqrs"
        const val FRIEND_SHIP_SEARCH = "friendship/search/{state}"
        const val SEND_MESSAGE = "messages"
        const val GET_MESSAGES = "messages/getmessagesbyfriendship/{contact_id}"
        const val GET_QUESTIONS = "questions"
        const val SEND_QUESTIONS = "inforecovery"
        const val GET_RECOVERY_QUESTIONS = "inforecovery/getanswersinforecovery/{nick}"
        const val SEND_ANSWERS = "inforecovery/validateanswers"
    }

    enum class AccountStatus constructor(val id: Int) {
        CODE_VALIDATED(1),
        ACCOUNT_CREATED(2),
        ACCOUNT_RECOVERED(3)
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
        TEN_SECONDS(1),
        THIRTY_SECONDS(2),
        ONE_MINUTE(3),
        TWO_MINUTES(4),
        FIVE_MINUTES(5),
        IMMEDIATELY(6)
    }

    enum class AllowDownloadAttachments constructor(val option: Int) {
        YES(1),
        NO(2)
    }

    enum class RecoveryQuestionsSaved constructor(val id: Int) {
        SAVED_QUESTIONS(1)
    }

    enum class TypeDialog constructor(val option: Int) {
        ALERT(1),
        INFO(2)
    }

    enum class FriendShipState constructor(val state: String) {
        ACTIVE("active"),
        BLOCKED("block")
    }

    enum class IsMine constructor(val value: Int) {
        YES(1),
        NO(0)
    }

    enum class ConversationAttachmentType(val type: String) {
        IMAGE("image"),
        AUDIO("audio"),
        VIDEO("video"),
        WORD("word"),
        EXCEL("excel"),
        PDF("pdf")
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
        const val PREF_ALLOW_DOWNLOAD_ATTACHMENTS = "allow_download_attachments"
        const val PREF_SOCKET_ID = "socket_id"
        const val PREF_RECOVERY_QUESTIONS_SAVED = "recovery_questions_saved"
    }

    object RegularExpressions {
        const val ONLY_LETTERS_AND_NUMBERS = "^[a-zA-Z0-9]*$"
        const val NICKNAME = "^[a-zA-Z0-9]{5,20}$"
        const val NAME_TO_SHOW = "^[a-zA-ZñÑ]+(([' ][a-zA-ZñÑ ])?[a-zA-Z]){5,50}$"
    }
}