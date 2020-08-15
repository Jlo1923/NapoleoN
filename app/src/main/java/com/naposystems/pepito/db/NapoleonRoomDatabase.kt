package com.naposystems.pepito.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naposystems.pepito.db.dao.contact.ContactDao
import com.naposystems.pepito.db.dao.message.MessageDao
import com.naposystems.pepito.db.dao.attachment.AttachmentDao
import com.naposystems.pepito.db.dao.quoteMessage.QuoteDao
import com.naposystems.pepito.db.dao.status.StatusDao
import com.naposystems.pepito.db.dao.user.UserDao
import com.naposystems.pepito.entity.*
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.Quote
import com.naposystems.pepito.entity.message.attachments.Attachment

@Database(
    entities = [
        User::class, Status::class, Message::class,
        Attachment::class, Contact::class,
        Quote::class
    ],
    version = 13
)
abstract class NapoleonRoomDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun statusDao(): StatusDao

    abstract fun messageDao(): MessageDao

    abstract fun quoteMessageDao(): QuoteDao

    abstract fun attachmentDao(): AttachmentDao

    abstract fun contactDao(): ContactDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user ADD image_url TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user ADD status TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user ADD header_uri TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE `blocked_contacts` (
                        `id` INTEGER NOT NULL,
                        `image_url` TEXT NOT NULL,
                        `nickname` TEXT NOT NULL,
                        `display_name` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `last_seen` TEXT NOT NULL,
                        PRIMARY KEY (`id`)
                        )""".trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user ADD chat_background TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE `conversation` (
                        `id` TEXT NOT NULL,
                        `body` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `user_destination` INTEGER NOT NULL,
                        `contact_id` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        PRIMARY KEY (`id`)
                        )""".trimIndent()
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE conversation ADD is_mine INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE conversation ADD channel_name TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE `conversation_attachment` (
                        `id` INTEGER NOT NULL,
                        `message_id` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `body` TEXT NOT NULL,
                        PRIMARY KEY (`id`)
                        )""".trimIndent()
                )
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE `contact` (
                        `id` INTEGER NOT NULL,
                        `image_url` TEXT NOT NULL,
                        `nickname` TEXT NOT NULL,
                        `display_name` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `last_seen` TEXT NOT NULL,
                        PRIMARY KEY (`id`)
                        )""".trimIndent()
                )
            }
        }
    }

}