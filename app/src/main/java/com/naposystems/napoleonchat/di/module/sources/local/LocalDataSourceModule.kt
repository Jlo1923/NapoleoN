package com.naposystems.napoleonchat.di.module.sources.local

import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.datasource.messageNotSent.MessageNotSentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.messageNotSent.MessageNotSentLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.datasource.quoteMessage.QuoteLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.quoteMessage.QuoteLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.datasource.status.StatusLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.status.StatusLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import dagger.Binds
import dagger.Module

@Module
abstract class LocalDataSourceModule {

    @Binds
    abstract fun bindUserLocalDataSource(dataSource: UserLocalDataSourceImp): UserLocalDataSource

    @Binds
    abstract fun bindStatusLocalDataSource(dataSource: StatusLocalDataSourceImp): StatusLocalDataSource

    @Binds
    abstract fun bindQuoteLocalDataSource(dataSource: QuoteLocalDataSourceImp): QuoteLocalDataSource

    @Binds
    abstract fun bindMessageLocalDataSource(dataSource: MessageLocalDataSourceImp): MessageLocalDataSource

    @Binds
    abstract fun bindContactLocalDataSource(dataSource: ContactLocalDataSourceImp): ContactLocalDataSource

    @Binds
    abstract fun bindAttachmentLocalDataSource(dataSource: AttachmentLocalDataSourceImp): AttachmentLocalDataSource

    @Binds
    abstract fun bindMessageNotSentLocalDataSource(dataSource: MessageNotSentLocalDataSourceImp): MessageNotSentLocalDataSource

}