package com.naposystems.pepito.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naposystems.pepito.db.dao.blockedContacts.BlockedContactDao
import com.naposystems.pepito.db.dao.status.StatusDao
import com.naposystems.pepito.db.dao.user.UserDao
import com.naposystems.pepito.entity.BlockedContact
import com.naposystems.pepito.entity.Status
import com.naposystems.pepito.entity.User

@Database(entities = [User::class, Status::class, BlockedContact::class], version = 5)
abstract class NapoleonRoomDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun statusDao(): StatusDao

    abstract fun blockedContactDao(): BlockedContactDao

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
    }

}