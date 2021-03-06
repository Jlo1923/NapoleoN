package com.naposystems.napoleonchat.di.module.sources.local

import com.naposystems.napoleonchat.source.local.db.NapoleonRoomDatabase
import com.naposystems.napoleonchat.source.local.dao.AttachmentDao
import com.naposystems.napoleonchat.source.local.dao.ContactDao
import com.naposystems.napoleonchat.source.local.dao.MessageDao
import com.naposystems.napoleonchat.source.local.dao.MessageNotSentDao
import com.naposystems.napoleonchat.source.local.dao.QuoteDao
import com.naposystems.napoleonchat.source.local.dao.StatusDao
import com.naposystems.napoleonchat.source.local.dao.UserDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DaoModule {

    @Provides
    fun provideUserDao(napoleonRoomDatabase: NapoleonRoomDatabase): UserDao {
        return napoleonRoomDatabase.userDao()
    }

    @Provides
    fun provideStatusDao(napoleonRoomDatabase: NapoleonRoomDatabase): StatusDao {
        return napoleonRoomDatabase.statusDao()
    }

    @Provides
    fun provideMessageDao(napoleonRoomDatabase: NapoleonRoomDatabase): MessageDao {
        return napoleonRoomDatabase.messageDao()
    }

    @Provides
    fun provideQuoteDao(napoleonRoomDatabase: NapoleonRoomDatabase): QuoteDao {
        return napoleonRoomDatabase.quoteDao()
    }

    @Provides
    fun provideAttachmentDao(napoleonRoomDatabase: NapoleonRoomDatabase): AttachmentDao {
        return napoleonRoomDatabase.attachmentDao()
    }

    @Provides
    fun provideContactDao(napoleonRoomDatabase: NapoleonRoomDatabase): ContactDao {
        return napoleonRoomDatabase.contactDao()
    }

    @Provides
    fun provideMessageNotSentDao(napoleonRoomDatabase: NapoleonRoomDatabase): MessageNotSentDao {
        return napoleonRoomDatabase.messageNotSentDao()
    }

}