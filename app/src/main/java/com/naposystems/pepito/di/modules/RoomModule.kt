package com.naposystems.pepito.di.modules

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naposystems.pepito.R
import com.naposystems.pepito.db.NapoleonRoomDatabase
import com.naposystems.pepito.db.dao.contact.ContactDao
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.contact.ContactLocalDataSource
import com.naposystems.pepito.db.dao.message.MessageDao
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.message.MessageLocalDataSource
import com.naposystems.pepito.db.dao.attachment.AttachmentDao
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.attachment.AttachmentLocalDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDao
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationLocalDataSource
import com.naposystems.pepito.db.dao.quoteMessage.QuoteDao
import com.naposystems.pepito.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.pepito.db.dao.quoteMessage.QuoteLocalDataSource
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
                    NapoleonRoomDatabase.MIGRATION_1_2
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        val prepopulateData = listOf(
                            Status(0, R.string.text_status_available),
                            Status(0, R.string.text_status_busy),
                            Status(0, R.string.text_status_in_meeting),
                            Status(0, R.string.text_status_only_messages),
                            Status(0, R.string.text_status_sleeping),
                            Status(0, R.string.text_status_only_emergency)
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
    fun provideMessageDao(napoleonRoomDatabase: NapoleonRoomDatabase): MessageDao {
        return napoleonRoomDatabase.messageDao()
    }

    @Provides
    @Singleton
    fun provideQuoteMessageDao(napoleonRoomDatabase: NapoleonRoomDatabase): QuoteDao {
        return napoleonRoomDatabase.quoteMessageDao()
    }

    @Provides
    @Singleton
    fun provideQuoteLocalDataSource(quoteDao: QuoteDao): QuoteDataSource {
        return QuoteLocalDataSource(quoteDao)
    }

    @Provides
    @Singleton
    fun provideMessageLocalDataSource(messageDao: MessageDao): MessageDataSource {
        return MessageLocalDataSource(messageDao)
    }

    @Provides
    @Singleton
    fun provideAttachmentDao(napoleonRoomDatabase: NapoleonRoomDatabase): AttachmentDao {
        return napoleonRoomDatabase.attachmentDao()
    }

    @Provides
    @Singleton
    fun provideAttachmentLocalDataSource(attachmentDao: AttachmentDao): AttachmentDataSource {
        return AttachmentLocalDataSource(attachmentDao)
    }

    @Provides
    @Singleton
    fun provideContactDao(napoleonRoomDatabase: NapoleonRoomDatabase): ContactDao {
        return napoleonRoomDatabase.contactDao()
    }

    @Provides
    @Singleton
    fun provideContactLocalDataSource(contactDao: ContactDao): ContactDataSource {
        return ContactLocalDataSource(contactDao)
    }

    @Provides
    @Singleton
    fun provideConversationDao(napoleonRoomDatabase: NapoleonRoomDatabase): ConversationDao {
        return napoleonRoomDatabase.conversationDao()
    }

    @Provides
    @Singleton
    fun provideConversationLocalDataSource(contactDao: ConversationDao): ConversationDataSource {
        return ConversationLocalDataSource(contactDao)
    }
}