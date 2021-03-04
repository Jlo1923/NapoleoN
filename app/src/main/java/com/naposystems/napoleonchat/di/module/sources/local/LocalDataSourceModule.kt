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

@Module
abstract class LocalDataSourceModule {

    @Binds
    abstract fun bindUserLocalDataSource(dataSource: UserLocalDataSource): UserDataSource

    @Binds
    abstract fun bindStatusLocalDataSource(dataSource: StatusLocalDataSource): StatusDataSource

    @Binds
    abstract fun bindQuoteLocalDataSource(dataSource: QuoteLocalDataSource): QuoteDataSource

    @Binds
    abstract fun bindMessageLocalDataSource(dataSource: MessageLocalDataSource): MessageDataSource

    @Binds
    abstract fun bindContactLocalDataSource(dataSource: ContactLocalDataSource): ContactDataSource

    @Binds
    abstract fun bindAttachmentLocalDataSource(dataSource: AttachmentLocalDataSource): AttachmentDataSource

    @Binds
    abstract fun bindMessageNotSentLocalDataSource(dataSource: MessageNotSentLocalDataSource): MessageNotSentDataSource

}