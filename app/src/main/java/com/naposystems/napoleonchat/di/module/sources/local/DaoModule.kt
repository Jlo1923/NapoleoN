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