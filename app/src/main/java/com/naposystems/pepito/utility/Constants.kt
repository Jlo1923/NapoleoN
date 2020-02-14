package com.naposystems.pepito.utility

object Constants {
    const val URL_TERMS_AND_CONDITIONS = "https://napoleonsecretchat.com/privacidad/"
    const val URL_FREQUENT_QUESTIONS = "https://napoleonsecretchat.com/privacidad/"
    const val DATA_CRYPT = "datacrypt"

    object NapoleonApi {
        const val BASE_URL = "http://192.168.1.222/nn-backend-secret-chat/public/api/"
        const val SOCKET_BASE_URL = "http://192.168.1.222:6001"
        const val GENERATE_CODE = "auth/sendverificationcode"
        const val VERIFICATE_CODE = "auth/validateverificationcode"
        const val VALIDATE_NICKNAME = "auth/validatenick"
        const val CREATE_ACCOUNT = "users"
        const val UPDATE_USER_INFO = "users/update"
        const val UPDATE_MUTE_CONVERSATION = "friendship/silence/{id}"
        const val SEND_PQRS = "pqrs"
        const val FRIEND_SHIP_SEARCH = "friendship/search/{state}"
        const val FRIEND_SHIP_SEARCH_BY_DATE = "friendship/search/{state}"
        const val SEND_MESSAGE = "messages"
        const val GET_MY_MESSAGES = "messages/getmymessages"
        const val VERIFY_MESSAGES_RECEIVED = "messages/verifymessagesreceived"
        const val VERIFY_MESSAGES_READ = "messages/verifymessagesreaded"
        const val SEND_MESSAGES_READ = "messages/sendmessagesreaded"
        const val GET_QUESTIONS = "questions"
        const val SEND_QUESTIONS = "inforecovery"
        const val GET_RECOVERY_QUESTIONS = "inforecovery/getanswersinforecovery/{nick}"
        const val SEND_ANSWERS = "inforecovery/validateanswers"
        const val SEARCH_USER = "users/search/{nick}"
        const val SEND_FRIENDSHIP_REQUEST = "friendshiprequest"
        const val GET_FRIENDSHIP_REQUESTS = "friendshiprequest"
        const val PUT_FRIENDSHIP_REQUEST = "friendshiprequest/{id}"
        const val GET_FRIENDSHIP_REQUEST_QUANTITY = "friendshiprequest/countfriendshiprequest"
        const val PUT_BLOCK_CONTACT = "friendship/blockuser/{id}"
        const val DELETE_CONTACT = "friendship/{id}"
        const val PUT_UNBLOCK_CONTACT = "friendship/unblockuser/{id}"
    }

    enum class ColorScheme constructor(val scheme: Int) {
        LIGHT_THEME(1),
        DARK_THEME(2)
    }

    enum class OutputControl constructor(val state: Int) {
        TRUE(1),
        FALSE(0)
    }

    enum class AccountStatus constructor(val id: Int) {
        CODE_VALIDATED(1),
        ACCOUNT_CREATED(2),
        ACCOUNT_RECOVERED(3)
    }

    enum class Biometrics constructor(val option: Int) {
        WITHOUT_BIOMETRICS(1),
        UNLOCK_WITH_FINGERPRINT(2),
        UNLOCK_WITH_FACEID(3),
        BIOMETRICS_NOT_FOUND(4)
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
        IMMEDIATELY(1),
        TEN_SECONDS(10000),
        THIRTY_SECONDS(30000),
        ONE_MINUTE(60000),
        FIVE_MINUTES(300000),
        ONE_HOUR(3600000),
        ONE_DAY(86400000),
        NEVER(-1)
    }

    enum class TimeMuteConversation constructor(val time: Int) {
        WITHOUT_TIME(0),
        ONE_HOUR(60),
        EIGHT_HOURS(480),
        ONE_DAY(1),
        ONE_YEAR(365)
    }

    enum class LockTypeApp constructor(val type: Int) {
        LOCK_FOR_TIME_REQUEST_PIN(1),
        LOCK_APP_FOR_ATTEMPTS(2),
        FOREVER_UNLOCK(3)
    }

    enum class LockStatus constructor(val state: Int) {
        LOCK(1),
        UNLOCK(2)
    }

    enum class TotalAttempts constructor(val attempts: Int) {
        ATTEMPTS_ONE(1),
        ATTEMPTS_TWO(2),
        ATTEMPTS_THREE(3),
        ATTEMPTS_FOUR(4)
    }

    enum class TimeUnlockApp constructor(val time: Long) {
        THIRTY_SECONDS(30000),
        FIVE_MINUTES(300000),
        TWENTY_MINUTES(1200000),
        ONE_HOUR(3600000),
        ONE_DAY(86400000)
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

    enum class AttachmentType(val type: String) {
        IMAGE("image"),
        AUDIO("audio"),
        VIDEO("video"),
        WORD("word"),
        EXCEL("excel"),
        PDF("pdf")
    }

    enum class FriendShipRequestType(val type: Int) {
        TITLE(0),
        FRIENDSHIP_REQUEST_RECEIVED(1),
        FRIENDSHIP_REQUEST_OFFER(2)
    }

    enum class FriendshipRequestPutAction(val action: String) {
        ACCEPT("accepted"),
        REFUSE("rejected"),
        CANCEL("cancel")
    }

    enum class NotificationType(val type: Int) {
        NEW_FRIENDSHIP_REQUEST(2)
    }

    enum class MessageStatus(val status: Int) {
        SENT(1),
        UNREAD(2),
        READED(3)
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
        const val PREF_CONTACTS_UPDATE_DATE = "contacts_update_date"
        const val PREF_SECRET_KEY = "secret_key"
        const val PREF_OUTPUT_CONTROL = "output_control"

        //region Lock and Unlock App
        const val PREF_LOCK_STATUS = "lock_status"
        const val PREF_TYPE_LOCK_APP = "type_lock_app"
        const val PREF_BIOMETRICS_OPTION = "option_biometrics"
        const val PREF_LOCK_TIME_APP = "lock_time_app"
        const val PREF_UNLOCK_TIME_APP = "unlock_time_app"
        const val PREF_UNLOCK_ATTEMPTS = "unlock_attempts"
        const val PREF_UNLOCK_TOTAL_ATTEMPTS = "unlock_total_attempts"
        //endregion
    }

    object RegularExpressions {
        const val ONLY_LETTERS_AND_NUMBERS = "^[a-zA-Z0-9]*$"
        const val NICKNAME = "^[a-zA-Z0-9]{5,20}$"
        const val NAME_TO_SHOW = "^[a-zA-ZñÑ]+(([' ][a-zA-ZñÑ ])?[a-zA-Z]){5,50}$"
    }
}