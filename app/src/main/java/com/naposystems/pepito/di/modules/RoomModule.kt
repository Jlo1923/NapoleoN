package com.naposystems.pepito.di.modules

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naposystems.pepito.R
import com.naposystems.pepito.db.NapoleonRoomDatabase
import com.naposystems.pepito.db.dao.blockedContacts.BlockedContactDao
import com.naposystems.pepito.db.dao.blockedContacts.BlockedContactsLocalDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDao
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationLocalDataSource
import com.naposystems.pepito.db.dao.status.StatusDao
import com.naposystems.pepito.db.dao.status.StatusLocalDataSource
import com.naposystems.pepito.db.dao.user.UserDao
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.entity.Status
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
class RoomModule {

    lateinit var napoleonDB: NapoleonRoomDatabase

    @Provides
    @Singleton
    fun provideRoomDatabase(context: Context): NapoleonRoomDatabase {
        napoleonDB =
            Room.databaseBuilder(context, NapoleonRoomDatabase::class.java, "napoleon_database")
                .addMigrations(
                    NapoleonRoomDatabase.MIGRATION_1_2,
                    NapoleonRoomDatabase.MIGRATION_2_3,
                    NapoleonRoomDatabase.MIGRATION_3_4,
                    NapoleonRoomDatabase.MIGRATION_4_5,
                    NapoleonRoomDatabase.MIGRATION_5_6,
                    NapoleonRoomDatabase.MIGRATION_6_7,
                    NapoleonRoomDatabase.MIGRATION_7_8,
                    NapoleonRoomDatabase.MIGRATION_8_9
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        val prepopulateData = listOf(
                            Status(0, R.string.text_status_available, ""),
                            Status(0, R.string.text_status_busy, ""),
                            Status(0, R.string.text_status_in_meeting, ""),
                            Status(0, R.string.text_status_only_messages, ""),
                            Status(0, R.string.text_status_sleeping, ""),
                            Status(0, R.string.text_status_only_emergency, "")
                        )

                        Executors.newSingleThreadExecutor().execute {
                            napoleonDB.statusDao().insertStatus(prepopulateData)
                        }
                    }
                })
                .allowMainThreadQueries()
                .build()

        return napoleonDB
    }

    @Provides
    @Singleton
    fun provideUserDao(napoleonRoomDatabase: NapoleonRoomDatabase): UserDao {
        return napoleonRoomDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideUserLocalDataSource(userDao: UserDao): UserLocalDataSource {
        return UserLocalDataSource(userDao)
    }

    @Provides
    @Singleton
    fun provideStatusDao(napoleonRoomDatabase: NapoleonRoomDatabase): StatusDao {
        return napoleonRoomDatabase.statusDao()
    }

    @Provides
    @Singleton
    fun provideStatusLocalDataSource(statusDao: StatusDao): StatusLocalDataSource {
        return StatusLocalDataSource(statusDao)
    }

    @Provides
    @Singleton
    fun provideBlockedContactDao(napoleonRoomDatabase: NapoleonRoomDatabase): BlockedContactDao {
        return napoleonRoomDatabase.blockedContactDao()
    }

    @Provides
    @Singleton
    fun provideBlockedContactsLocalDataSource(blockedContactDao: BlockedContactDao): BlockedContactsLocalDataSource {
        return BlockedContactsLocalDataSource(blockedContactDao)
    }

    @Provides
    @Singleton
    fun provideConversationDao(napoleonRoomDatabase: NapoleonRoomDatabase): ConversationDao {
        return napoleonRoomDatabase.conversationDao()
    }

    @Provides
    @Singleton
    fun provideConversationLocalDataSource(conversationDao: ConversationDao): ConversationDataSource {
        return ConversationLocalDataSource(conversationDao)
    }
}