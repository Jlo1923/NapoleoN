package com.naposystems.napoleonchat.di.modules

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.db.NapoleonRoomDatabase
import com.naposystems.napoleonchat.db.dao.contact.ContactDao
import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDao
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageLocalDataSource
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDao
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDao
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteLocalDataSource
import com.naposystems.napoleonchat.db.dao.status.StatusDao
import com.naposystems.napoleonchat.db.dao.status.StatusLocalDataSource
import com.naposystems.napoleonchat.db.dao.user.UserDao
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.entity.Status
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
                            Status(1, context.getString(R.string.text_status_available)),
                            Status(2, context.getString(R.string.text_status_busy)),
                            Status(3, context.getString(R.string.text_status_in_meeting)),
                            Status(4, context.getString(R.string.text_status_only_messages)),
                            Status(5, context.getString(R.string.text_status_sleeping)),
                            Status(6, context.getString(R.string.text_status_only_emergency))
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
    fun provideUserLocalDataSource(
        userDao: UserDao,
        napoleonRoomDatabase: NapoleonRoomDatabase
    ): UserLocalDataSource {
        return UserLocalDataSource(napoleonRoomDatabase, userDao)
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
    fun provideMessageLocalDataSource(context: Context, messageDao: MessageDao): MessageDataSource {
        return MessageLocalDataSource(context, messageDao)
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

}