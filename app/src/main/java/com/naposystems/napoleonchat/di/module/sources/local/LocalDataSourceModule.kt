package com.naposystems.napoleonchat.di.module.sources.local

import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageLocalDataSource
import com.naposystems.napoleonchat.db.dao.messageNotSent.MessageNotSentDataSource
import com.naposystems.napoleonchat.db.dao.messageNotSent.MessageNotSentLocalDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteLocalDataSource
import com.naposystems.napoleonchat.db.dao.status.StatusDataSource
import com.naposystems.napoleonchat.db.dao.status.StatusLocalDataSource
import com.naposystems.napoleonchat.db.dao.user.UserDataSource
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class LocalDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindUserLocalDataSource(dataSource: UserLocalDataSource): UserDataSource

    @Binds
    @Singleton
    abstract fun bindStatusLocalDataSource(dataSource: StatusLocalDataSource): StatusDataSource

    @Binds
    @Singleton
    abstract fun bindQuoteLocalDataSource(dataSource: QuoteLocalDataSource): QuoteDataSource

    @Binds
    @Singleton
    abstract fun bindMessageLocalDataSource(dataSource: MessageLocalDataSource): MessageDataSource

    @Binds
    @Singleton
    abstract fun bindContactLocalDataSource(dataSource: ContactLocalDataSource): ContactDataSource

    @Binds
    @Singleton
    abstract fun bindAttachmentLocalDataSource(dataSource: AttachmentLocalDataSource): AttachmentDataSource

    @Binds
    @Singleton
    abstract fun bindMessageNotSentLocalDataSource(dataSource: MessageNotSentLocalDataSource): MessageNotSentDataSource

}