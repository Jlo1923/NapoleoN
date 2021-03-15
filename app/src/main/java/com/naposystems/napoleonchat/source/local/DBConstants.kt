package com.naposystems.napoleonchat.source.local

object DBConstants {
    object User {
        const val TABLE_NAME_USER = "user"
        const val COLUMN_ID = "id"
        const val COLUMN_FIREBASE_ID = "firebase_id"
        const val COLUMN_NICKNAME = "nickname"
        const val COLUMN_DISPLAY_NAME = "display_name"
        const val COLUMN_ACCESS_PIN = "access_pin"
        const val COLUMN_IMAGE_URL = "image_url"
        const val COLUMN_STATUS = "status"
        const val COLUMN_HEADER_URI = "header_uri"
        const val COLUMN_CHAT_BACKGROUND = "chat_background"
        const val COLUMN_TYPE = "type"
        const val COLUMN_CREATED_AT = "create_at"
    }

    object Status {
        const val TABLE_NAME_STATUS = "status"
        const val COLUMN_ID = "id"
        const val COLUMN_STATUS = "status"
        const val COLUMN_CUSTOM_STATUS = "custom_status"
    }

    object MessageNotSent {
        const val TABLE_NAME_MESSAGE_NOT_SENT = "message_not_sent"
        const val COLUMN_ID = "id"
        const val COLUMN_MESSAGE = "message"
        const val COLUMN_CONTACT_ID = "contact_id"
    }

    object Contact {
        const val TABLE_NAME_CONTACT = "contact"
        const val COLUMN_ID = "id"
        const val COLUMN_IMAGE_URL = "image_url"
        const val COLUMN_IMAGE_URL_FAKE = "image_url_fake"
        const val COLUMN_NICKNAME = "nickname"
        const val COLUMN_NICKNAME_FAKE = "nickname_fake"
        const val COLUMN_DISPLAY_NAME = "display_name"
        const val COLUMN_DISPLAY_NAME_FAKE = "display_name_fake"
        const val COLUMN_STATUS = "status"
        const val COLUMN_LAST_SEEN = "last_seen"
        const val COLUMN_STATUS_BLOCKED = "status_blocked"
        const val COLUMN_SILENCED = "silenced"
        const val COLUMN_SELF_DESTRUCT_TIME = "self_destruct_time"
        const val COLUMN_STATE_NOTIFICATION = "state_notification"
        const val COLUMN_NOTIFICATION_ID = "notification_id"
    }

    object Message {
        const val TABLE_NAME_MESSAGE = "message"
        const val COLUMN_ID = "id"
        const val COLUMN_WEB_ID = "web_id"
        const val COLUMN_UUID = "uuid"
        const val COLUMN_BODY = "body"
        const val COLUMN_QUOTED = "quoted"
        const val COLUMN_CONTACT_ID = "contact_id"
        const val COLUMN_UPDATED_AT = "updated_at"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_IS_MINE = "is_mine"
        const val COLUMN_STATUS = "status"
        const val COLUMN_IS_SELECTED = "is_selected"
        const val COLUMN_NUMBER_ATTACHMENTS = "number_attachments"
        const val COLUMN_SELF_DESTRUCTION_AT = "self_destruction_at"
        const val COLUMN_TOTAL_SELF_DESTRUCTION_AT = "total_self_destruction_at"
        const val COLUMN_TYPE_MESSAGE = "type_message"
        const val COLUMN_CYPHER = "cypher"
    }

    object Attachment {
        const val TABLE_NAME_ATTACHMENT = "attachment"
        const val COLUMN_ID = "id"
        const val COLUMN_MESSAGE_ID = "message_id"
        const val COLUMN_WEB_ID = "web_id"
        const val COLUMN_MESSAGE_WEB_ID = "message_web_id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_BODY = "body"
        const val COLUMN_FILENAME = "filename"
        const val COLUMN_ORIGIN = "origin"
        const val COLUMN_THUMBNAIL_URI = "thumbnail_uri"
        const val COLUMN_STATUS = "status"
        const val COLUMN_EXTENSION = "extension"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_IS_COMPRESSED = "is_compressed"
    }

    object Quote {
        const val TABLE_NAME_QUOTE = "quote"
        const val COLUMN_ID = "id"
        const val COLUMN_MESSAGE_ID = "message_id"
        const val COLUMN_CONTACT_ID = "contact_id"
        const val COLUMN_BODY = "body"
        const val COLUMN_ATTACHMENT_TYPE = "attachment_type"
        const val COLUMN_THUMBNAIL_URI = "thumbnail_uri"
        const val COLUMN_MESSAGE_PARENT_ID = "message_parent_id"
        const val COLUMN_IS_MINE = "is_mine"
    }

}