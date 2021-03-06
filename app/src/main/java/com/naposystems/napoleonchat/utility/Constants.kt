package com.naposystems.napoleonchat.utility

import java.io.Serializable

object Constants {

    const val URL_TERMS_AND_CONDITIONS = "https://napoleon.chat/terminos-condiciones/"
    const val URL_FREQUENT_QUESTIONS = "https://napoleon.chat/faqs"
    const val GIPHY_API_KEY = "R6CTqCVGyxWIAEad1tbbRFouJ6C6hFQX"
    const val DATA_CRYPT = "datacrypt"
    const val REMOTE_CONFIG_EMOJIS_KEY = "Emojis"
    const val REMOTE_CONFIG_VERSION_KEY = "version_android"
    const val REMOTE_CONFIG_VERSION_CODE_KEY = "version_code_android"
    const val QUANTITY_TO_SHOW_FAB_CONVERSATION = 3
    const val QUANTITY_MIN_TO_SHOW_ACTIONMODE = 1
    const val QUANTITY_TO_HIDE_ACTIONMODE = 0
    const val QUANTITY_ATTACHMENTS = 0
    const val MAX_AUDIO_RECORD_TIME = 1800000L
    const val MAX_IMAGE_VIDEO_FILE_SIZE = 20 * 1048576
    const val MAX_DOCUMENT_FILE_SIZE = 100 * 1048576

    object CallKeys {
        const val CALL_MODEL = "callModel"
        const val CHANNEL_NAME = "channel_private"
        const val CONTACT_ID = "contact_id"
        const val IS_VIDEO_CALL = "is_videocall"
        const val OFFER = "offer"
    }

    object ParamsOffer {

        const val PARAM_TWO_ORIGINAL = "na=extmap"
        const val PARAM_TWO_REPLACE = "#$#"

        const val PARAM_THREE_ORIGINAL = "na=rtpmap"
        const val PARAM_THREE_REPLACE = "#%#"

        const val PARAM_FOUR_ORIGINAL = "na=rtcp-fb"
        const val PARAM_FOUR_REPLACE = "#&#"

        const val PARAM_FIVE_ORIGINAL = "na=ssrc"
        const val PARAM_FIVE_REPLACE = "#?#"

    }

    object NotificationKeys {
        const val TYPE_NOTIFICATION = "type_notification"
        const val MESSAGE_ID = "message_id"
        const val BODY = "body"
        const val TITLE = "title"
        const val CONTACT = "contact"
        const val ATTACKER_ID = "attacker_id"
        const val MESSAGE = "message"
        const val BADGE = "badge"
        const val SOUND = "sound"
        const val SILENCE = "silence"
    }

    object RegularExpressions {
        const val ONLY_LETTERS_AND_NUMBERS = "^[a-zA-Z0-9]*$"
        const val NICKNAME = "^[a-zA-Z0-9]{5,20}$"
        const val NAME_TO_SHOW = "^[a-zA-Z????]+(([' ][a-zA-Z???? ])?[a-zA-Z]){5,50}$"
    }

    object SharedPreferences {
        const val PREF_NAME = "napoleon_preferences"
        const val PREF_LANGUAGE_SELECTED = "language_selected"
        const val PREF_ACCOUNT_STATUS = "account_status"
        const val PREF_FIREBASE_ID = "firebase_id"
        const val PREF_USER_ID = "user_id"
        const val PREF_USER_CREATED_AT = "user_created_at"
        const val PREF_COLOR_SCHEME = "color_scheme"
        const val PREF_USER_DISPLAY_FORMAT = "user_display_format"
        const val PREF_TIME_FORMAT = "time_format"
        const val PREF_SELF_DESTRUCT_TIME = "self_destruct_time"
        const val PREF_TIME_REQUEST_ACCESS_PIN = "time_request_access_pin"
        const val PREF_ALLOW_DOWNLOAD_ATTACHMENTS = "allow_download_attachments"
        const val PREF_SOCKET_ID = "socket_id"
        const val PREF_SECRET_KEY = "secret_key"
        const val PREF_OUTPUT_CONTROL = "output_control"
        const val PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT = "message_self_destruct_time_not_sent"
        const val PREF_JSON_NOTIFICATION = "json_notification"
        const val PREF_LAST_JSON_NOTIFICATION = "last_json_notification"
        const val PREF_ATTEMPTS_FOR_NEW_CODE = "attempts_for_new_code"
        const val PREF_TIME_FOR_NEW_CODE = "time_for_new_code"
        const val PREF_ATTEMPTS_FOR_RETRY_CODE = "attempts_for_retry_code"
        const val PREF_TIME_FOR_RETRY_CODE = "time_for_retry_code"
        const val PREF_CHANNEL_CREATED = "channel_created"

        const val PREF_NOTIFICATION_MESSAGE_CHANNEL_ID = "notification_message_channel_id"
        const val PREF_NOTIFICATION_GROUP_CHANNEL_ID = "notification_group_channel_id"

        //region RecoveryAccount
        const val PREF_ACCOUNT_RECOVERY_ATTEMPTS = "account_recovery_attempts"
        const val PREF_RECOVERY_QUESTIONS_SAVED = "recovery_questions_saved"
        const val PREF_EXISTING_ATTACK = "existing_attack"
        const val PREF_ATTACKER_ID = "attacker_id"
        //endregion

        //region Subscription
        const val PREF_DIALOG_SUBSCRIPTION = "DIALOG_SUBSCRIPTION"
        const val PREF_FREE_TRIAL = "free_trial"
        const val PREF_TYPE_SUBSCRIPTION = "type_subscription"
        const val PREF_SUBSCRIPTION_TIME = "subscription_time"
        const val SubscriptionStatus = "subscription_status"
        //endregion

        //region Lock and Unlock App
        const val PREF_LOCK_STATUS = "lock_status"
        const val PREF_TYPE_LOCK_APP = "type_lock_app"
        const val PREF_BIOMETRICS_OPTION = "option_biometrics"
        const val PREF_LOCK_TIME_APP = "lock_time_app"
        const val PREF_UNLOCK_TIME_APP = "unlock_time_app"
        const val PREF_UNLOCK_ATTEMPTS = "unlock_attempts"
        const val PREF_UNLOCK_TOTAL_ATTEMPTS = "unlock_total_attempts"
        //endregion

        //region
        const val PREF_SHOW_CASE_FIRST_STEP_HAS_BEEN_SHOW = "show_case_first_step_has_been_show"
        const val PREF_SHOW_CASE_SECOND_STEP_HAS_BEEN_SHOW = "show_case_second_step_has_been_show"
        const val PREF_SHOW_CASE_THIRD_STEP_HAS_BEEN_SHOW = "show_case_third_step_has_been_show"
        const val PREF_SHOW_CASE_FOURTH_STEP_HAS_BEEN_SHOW = "show_case_fourth_step_has_been_show"
        const val PREF_SHOW_CASE_FIFTH_STEP_HAS_BEEN_SHOW = "show_case_fifth_step_has_been_show"
        const val PREF_SHOW_CASE_SIXTH_STEP_HAS_BEEN_SHOW = "show_case_sixth_step_has_been_show"
        const val PREF_SHOW_CASE_SEVENTH_STEP_HAS_BEEN_SHOW = "show_case_seventh_step_has_been_show"

        const val URIS_CACHE = "URIS_CACHE"
        const val WAS_IN_PREVIEW = "WAS_IN_PREVIEW"
        const val NAV_TO_CONTACTS = "NAV_TO_CONTACTS"
        //endregion
    }

    object ValidConnection {
        const val REQUEST_PIN = "ping -c 1 www.google.com"
    }

    //region A
    enum class AccountStatus constructor(val id: Int) {
        NO_ACCOUNT(0),
        CODE_VALIDATED(1),
        ACCOUNT_CREATED(2),
        ACCOUNT_RECOVERED(3)
    }

    enum class AddContactTitleType(val type: Int) {
        TITLE_MY_CONTACTS(1),
        TITLE_COINCIDENCES(2)
    }

    enum class AllowDownloadAttachments constructor(val option: Int) {
        YES(1),
        NO(2)
    }

    enum class AttachmentOrigin(val origin: Int) {
        CAMERA(1),
        GALLERY(2),
        AUDIO_SELECTION(3),
        DOWNLOADED(4),
        LOCATION(5),
        RECORD_AUDIO(6)
    }

    enum class MessageStatus(val status: Int) {
        SENDING(1),
        SENT(2),
        UNREAD(3),
        READED(4),
        ERROR(5)
    }

    enum class AttachmentStatus(val status: Int) {
        SENDING(1),
        SENT(2),
        ERROR(3),
        NOT_DOWNLOADED(4),
        DOWNLOADING(5),
        DOWNLOAD_COMPLETE(6),
        DOWNLOAD_ERROR(7),
        DOWNLOAD_CANCEL(8),
        UPLOAD_CANCEL(9),
        RECEIVED(10),
        READED(11),
    }

    enum class AttachmentType(val type: String) {
        IMAGE("image"),
        AUDIO("audio"),
        VIDEO("video"),
        DOCUMENT("document"),
        GIF("gif"),
        GIF_NN("gifNN"),
        LOCATION("location")
    }
    //endregion

    //region B
    enum class Biometrics constructor(val option: Int) {
        WITHOUT_BIOMETRICS(1),
        UNLOCK_WITH_FINGERPRINT(2),
        UNLOCK_WITH_FACEID(3),
        BIOMETRICS_NOT_FOUND(4)
    }
    //endregion

    //region C
    enum class CacheDirectories(val folder: String) {
        IMAGES("Images"),
        VIDEOS("Videos"),
        AUDIOS("Audios"),
        DOCUMENTS("Documentos"),
        GIFS("Gifs"),
        CHAT_BACKGROUND("chatBackground"),
        AVATAR("avatars"),
        HEADER("headers"),
        IMAGE_FAKE_CONTACT("fakeContact"),
        TEMPS("temps")
    }

    enum class ChangeParams constructor(val option: Int) {
        NAME_FAKE(1),
        NICKNAME_FAKE(2),
        NAME_USER(3)
    }

    enum class ChannelCreated constructor(val state: Int) {
        TRUE(1),
        FALSE(0)
    }

    enum class ChannelType constructor(val type: Int) {
        DEFAULT(1),
        CUSTOM(2)
    }

    enum class CodeHttp constructor(val code: Int) {
        OK(200),
        CREATED(201),
        ACCEPTED(202),
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        UNPROCESSABLE_ENTITY(422),
        INTERNAL_SERVER_ERROR(500),
        BAD_GATEWAY(502),
        SERVICE_UNAVAILABLE(503)
    }

    enum class ColorScheme constructor(val scheme: Int) {
        LIGHT_THEME(1),
        DARK_THEME(2),
        BLACK_GOLD_ALLOY(3),
        COLD_OCEAN(4),
        CAMOUFLAGE(5),
        PURPLE_BLUEBONNETS(6),
        PINK_DREAM(7),
        CLEAR_SKY(8)
    }
    //endregion

    //region D
    enum class DeleteMessages constructor(val option: Int) {
        BY_SELECTION(1),
        BY_UNRECEIVED(2),
        BY_UNREADS(3),
        BY_FAILED(5)
    }
    //endregion

    //region E
    enum class EmojiCategory(val category: Int) {
        SMILES_AND_PEOPLE(1),
        ANIMALS_AND_NATURE(2),
        FOOD_AND_DRINK(3),
        ACTIVITY(4),
        TRAVEL_AND_PLACES(5),
        OBJECTS(6),
        SYMBOLS(7),
        FLAGS(8)
    }

    enum class ExistingAttack(val type: Int) {
        NOT_EXISTING(1),
        EXISTING(2)
    }

    //endregion

    //region F
    enum class FreeTrialUsers(val time: Int) {
        THIRTY_DAYS(30)
    }

    enum class FriendshipRequestPutAction(val action: String) {
        ACCEPT("accepted"),
        REFUSE("rejected"),
        CANCEL("cancel")
    }

    enum class FriendShipRequestType(val type: Int) {
        TITLE(0),
        FRIENDSHIP_REQUEST_RECEIVED(1),
        FRIENDSHIP_REQUEST_OFFER(2)
    }

    enum class FriendShipState constructor(val state: String) {
        ACTIVE("active"),
        BLOCKED("block")
    }

    enum class FromClosedApp constructor(val type: Boolean) : Serializable {
        YES(true),
        NO(false)
    }

    //endregion

    //region H
    enum class HeadsetState(val state: Int) {
        UNKNOWN(-1),
        PLUGGED(1),
        UNPLUGGED(0)
    }
    //endregion

    //region I
    enum class IsMine constructor(val value: Int) {
        YES(1),
        NO(0)
    }
    //endregion

    //region L
    //TODO: Recomendacion unir todos los enum de location en una clase y segmentarlos en subclase
    enum class LocationAddContact(val location: Int) {
        HOME(1),
        CONTACTS(2)
    }

    enum class LocationAlertDialog(val location: Int) {
        CONVERSATION(1),
        CALL_ACTIVITY(2)
    }

    enum class LocationConnectSocket(val location: Boolean) {
        FROM_APP(false),
        FROM_NOTIFICATION(true)
    }

    enum class LocationEmptyState(val location: Int) {
        WITHOUT_LOCATION(0),
        ADD_CONTACT_FRIENDSHIP_REQUEST(1),
        ADD_CONTACT_SEARCH(2),
        BLOCKED_CONTACTS(3),
        CONTACTS(4),
        HOME(5)
    }

    enum class LocationGetContact constructor(val location: Int) {
        OTHER(0),
        BLOCKED(1)
    }

    enum class LocationImageSelectorBottomSheet(val location: Int) {
        WITHOUT_LOCATION(0),
        PROFILE(1),
        BANNER_PROFILE(2),
        CONTACT_PROFILE(3),
        APPEARANCE_SETTINGS(4),
        CONVERSATION(5)
    }

    enum class LocationSearchView(val location: Int) {
        OTHER(0),
        LOCATION(1)
    }

    enum class LocationSelectionLanguage(val location: Int) {
        LANDING(1),
        APPEARANCE_SETTINGS(2)
    }

    enum class LockStatus constructor(val state: Int) {
        LOCK(1),
        UNLOCK(2)
    }

    enum class LockTypeApp constructor(val type: Int) {
        LOCK_FOR_TIME_REQUEST_PIN(1),
        LOCK_APP_FOR_ATTEMPTS(2),
        FOREVER_UNLOCK(3)
    }
    //endregion

    //region M
    enum class MessageEventType(val status: Int) {
        UNREAD(1),
        READ(2)
    }

    //Este estado es el usado en el Backend Para Recibido/Leido
    //Difiere del Estado de Mensaje y del Adjunto en Local
    enum class StatusMustBe(val status: Int) {
        RECEIVED(1),
        READED(2)
    }

    //Enumerable que describe el tipo de mensaje de texto
    enum class MessageTextType(val type: Int) {
        NORMAL(1),
        MISSED_CALL(2),
        MISSED_VIDEO_CALL(3),
        NEW_CONTACT(4),
        GROUP_DATE(5)
    }

    //Enumerable que describe el tipo de mensaje recibido por el estado
    enum class MessageType(val type: Int) {
        TEXT(0),
        ATTACHMENT(1)
    }

    enum class MimeType(val type: String) {
        DOC("application/msword"),
        DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        XLS("application/vnd.ms-excel"),
        XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        PPT("application/vnd.ms-powerpoint"),
        PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
        PDF("application/pdf")
    }
    //endregion

    //region N
    enum class NotificationType(val type: Int) {
        WITHOUT_NOTIFICATION(0),
        ENCRYPTED_MESSAGE(1),
        NEW_FRIENDSHIP_REQUEST(2),
        FRIEND_REQUEST_ACCEPTED(3),
        VERIFICATION_CODE(4),
        SUBSCRIPTION(5),
        ACCOUNT_ATTACK(6),
        INCOMING_CALL(7),
        REJECT_CALL(8),
        CANCEL_CALL(9),
        USER_AVAILABLE_FOR_CALL(10)
    }

    //endregion

    //region O
    enum class OutputControl constructor(val state: Int) {
        TRUE(1),
        FALSE(0)
    }
    //endregion

    //region R
    enum class RecoveryQuestionsSaved constructor(val id: Int) {
        SAVED_QUESTIONS(1)
    }
    //endregion

    //region S
    enum class SelfDestructTime constructor(val time: Int) {
        EVERY_FIVE_SECONDS(0),
        EVERY_FIFTEEN_SECONDS(1),
        EVERY_THIRTY_SECONDS(2),
        EVERY_ONE_MINUTE(3),
        EVERY_TEN_MINUTES(4),
        EVERY_THIRTY_MINUTES(5),
        EVERY_ONE_HOUR(6),
        EVERY_TWELVE_HOURS(7),
        EVERY_ONE_DAY(8),
        EVERY_SEVEN_DAY(9),
        EVERY_TWENTY_FOUR_HOURS_ERROR(10),
        EVERY_SEVEN_DAYS_ERROR(11)
    }

    enum class ShowDialogSubscription(val option: Int) {
        YES(0),
        NO(1)
    }

    enum class SkuSubscriptions(val sku: String) {
        QUARTERLY("com.naposystems.napoleonchat.quarterly"),
        SEMIANNUAL("com.naposystems.napoleonchat.semiannual"),
        YEARLY("com.naposystems.napoleonchat.yearly")
    }

    enum class SocketIdNotExist constructor(val socket: String) {
        SOCKET_ID_NO_EXIST("NO_EXIST")
    }

    enum class SocketChannelName constructor(val channelName: String) {
        PRIVATE_GLOBAL_CHANNEL_NAME("private-global"),
        PRIVATE_GENERAL_CHANNEL_NAME("private-general.")
    }

    enum class SocketChannelStatus constructor(val status: Boolean) {
        SOCKET_CHANNEL_STATUS_CONNECTED(true),
        SOCKET_CHANNEL_STATUS_NOT_CONNECTED(false)
    }

    enum class SocketListenEvents(val event: String) {
        DISCONNECT("disconnect"),
        NEW_MESSAGE("App\\Events\\NewMessageEvent"),
        NOTIFY_MESSAGES_RECEIVED("App\\Events\\NotifyMessagesReceived"),
        CANCEL_OR_REJECT_FRIENDSHIP_REQUEST("App\\Events\\CancelOrRejectFriendshipRequestEvent"),
        NOTIFY_MESSAGE_READED("App\\Events\\NotifyMessageReaded"),
        SEND_MESSAGES_DESTROY("App\\Events\\SendMessagesDestroyEvent"),
        CALL_FRIEND("App\\Events\\CallFriendEvent"),
        REJECTED_CALL("App\\Events\\RejectedCallEvent"),
        CANCEL_CALL("App\\Events\\CancelCallEvent"),
        BLOCK_OR_DELETE_FRIENDSHIP("App\\Events\\BlockOrDeleteFrienshipEvent"),
        USER_AVAILABLE_FOR_CALL("App\\Events\\UserAvailableForCallEvent")
    }

    enum class SocketEmitTriggers(val trigger: String) {
        CLIENT_CONVERSATION("client-conversationNN"),
        CLIENT_CALL("client-callNN")
    }

    enum class StateFlag(val state: Int) {
        ON(1),
        OFF(0)
    }

    enum class StateMessage(val state: Int) {
        START(1),
        SUCCESS(2),
        ERROR(3)
    }

    enum class SubscriptionStatus(val state: String) {
        PENDING("Pending"),
        ACTIVE("Active"),
        SUSPENDED("Suspended"),
        CANCELLED("Cancelled"),
        EXPIRED("Expired")
    }

    enum class SubscriptionsTimeType(val subscription: Int) {
        QUARTERLY(3),
        SEMIANNUAL(6),
        YEARLY(1)
    }
    //endregion

    //region T
    enum class ThemesApplication constructor(val theme: Int) {
        LIGHT_NAPOLEON(1),
        DARK_NAPOLEON(2),
        BLACK_GOLD_ALLOY(3),
        COLD_OCEAN(4),
        CAMOUFLAGE(5),
        PURPLE_BLUEBONNETS(6),
        PINK_DREAM(7),
        CLEAR_SKY(8)
    }

    enum class TimeFormat constructor(val time: Int) {
        EVERY_TWENTY_FOUR_HOURS(1),
        EVERY_TWELVE_HOURS(2)
    }

    enum class TimeMuteConversation constructor(val time: Int) {
        WITHOUT_TIME(0),
        ONE_HOUR(60),
        EIGHT_HOURS(480),
        ONE_DAY(1),
        ONE_YEAR(365)
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

    enum class TimeSendCode(val time: Int) {
        TEN_SECONDS(10000),
        THIRTY_SECONDS(30000),
        FIVE_MINUTES(300000)
    }

    enum class TimeUnlockApp constructor(val time: Long) {
        THIRTY_SECONDS(30000),
        FIVE_MINUTES(300000),
        TWENTY_MINUTES(1200000),
        ONE_HOUR(3600000),
        ONE_DAY(86400000)
    }

    enum class TotalAttempts constructor(val attempts: Int) {
        ATTEMPTS_ONE(1),
        ATTEMPTS_TWO(2),
        ATTEMPTS_THREE(3),
        ATTEMPTS_FOUR(4)
    }

    enum class TypeDialog constructor(val option: Int) {
        ALERT(1),
        INFO(2)
    }

    enum class TypeCall constructor(val type: Int) : Serializable {
        IS_INCOMING_CALL(1),
        IS_OUTGOING_CALL(2)
    }
    //endregion

    //region U
    enum class UserNotExist constructor(val user: Int) {
        USER_NO_EXIST(-1)
    }

    enum class UserDisplayFormat constructor(val format: Int) {
        NAME_AND_NICKNAME(1),
        ONLY_NAME(2),
        ONLY_NICKNAME(3)
    }

    enum class UserType(val type: Int) {
        NEW_USER(1),
        OLD_USER(2)
    }
    //endregion

    //region v
    enum class Vibrate constructor(val type: Int) {
        DEFAULT(0),
        SOFT(1)
    }
    //endregion

}