package com.naposystems.napoleonchat.di.module.sources.local

import com.naposystems.napoleonchat.db.NapoleonRoomDatabase
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDao
import com.naposystems.napoleonchat.db.dao.contact.ContactDao
import com.naposystems.napoleonchat.db.dao.message.MessageDao
import com.naposystems.napoleonchat.db.dao.messageNotSent.MessageNotSentDao
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDao
import com.naposystems.napoleonchat.db.dao.status.StatusDao
import com.naposystems.napoleonchat.db.dao.user.UserDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DaoModule {

    @Provides
    @Singleton
    fun provideUserDao(napoleonRoomDatabase: NapoleonRoomDatabase): UserDao {
        return napoleonRoomDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideStatusDao(napoleonRoomDatabase: NapoleonRoomDatabase): StatusDao {
        return napoleonRoomDatabase.statusDao()
    }

    @Provides
    @Singleton
    fun provideMessageDao(napoleonRoomDatabase: NapoleonRoomDatabase): MessageDao {
        return napoleonRoomDatabase.messageDao()
    }

    @Provides
    @Singleton
    fun provideQuoteDao(napoleonRoomDatabase: NapoleonRoomDatabase): QuoteDao {
        return napoleonRoomDatabase.quoteDao()
    }

    @Provides
    @Singleton
    fun provideAttachmentDao(napoleonRoomDatabase: NapoleonRoomDatabase): AttachmentDao {
        return napoleonRoomDatabase.attachmentDao()
    }

    @Provides
    @Singleton
    fun provideContactDao(napoleonRoomDatabase: NapoleonRoomDatabase): ContactDao {
        return napoleonRoomDatabase.contactDao()
    }

    @Provides
    @Singleton
    fun provideMessageNotSentDao(napoleonRoomDatabase: NapoleonRoomDatabase): MessageNotSentDao {
        return napoleonRoomDatabase.messageNotSentDao()
    }

}