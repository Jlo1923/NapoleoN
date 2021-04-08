package com.naposystems.napoleonchat.source.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naposystems.napoleonchat.source.local.dao.*
import com.naposystems.napoleonchat.source.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        StatusEntity::class,
        MessageEntity::class,
        AttachmentEntity::class,
        ContactEntity::class,
        QuoteEntity::class,
        MessageNotSentEntity::class
    ],
    version = 3
)
abstract class NapoleonRoomDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun statusDao(): StatusDao

    abstract fun messageDao(): MessageDao

    abstract fun quoteDao(): QuoteDao

    abstract fun attachmentDao(): AttachmentDao

    abstract fun contactDao(): ContactDao

    abstract fun messageNotSentDao(): MessageNotSentDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE `message_not_sent` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `message` TEXT NOT NULL,
                        `contact_id` INTEGER NOT NULL,
                        FOREIGN KEY(contact_id) REFERENCES contact(id) ON UPDATE CASCADE ON DELETE CASCADE
                        )""".trimIndent()
                )

                database.execSQL("CREATE UNIQUE INDEX `index_message_not_sent_contact_id` ON `message_not_sent` (`contact_id`)")

                database.execSQL("ALTER TABLE contact ADD COLUMN 'state_notification' INTEGER NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE contact ADD COLUMN 'notification_id' TEXT DEFAULT NULL")

                database.execSQL("ALTER TABLE message ADD COLUMN 'uuid' TEXT DEFAULT NULL")

                database.execSQL("ALTER TABLE 'message' ADD COLUMN 'cypher' INTEGER NOT NULL DEFAULT 1")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("ALTER TABLE attachment ADD COLUMN 'self_destruction_at' INTEGER NOT NULL DEFAULT -1")

                database.execSQL("ALTER TABLE attachment ADD COLUMN 'total_self_destruction_at' INTEGER NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE attachment ADD COLUMN 'updated_at' INTEGER NOT NULL DEFAULT 0")

                database.execSQL(
                    """UPDATE attachment SET 
                    self_destruction_at = (SELECT self_destruction_at FROM message WHERE attachment.message_id = message.id), 
                    total_self_destruction_at = (SELECT total_self_destruction_at FROM message WHERE attachment.message_id = message.id), 
                    updated_at = (SELECT updated_at FROM message WHERE attachment.message_id = message.id)"""
                )

            }
        }
    }

}