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
import javax.inject.Singleton

@Module
abstract class LocalDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindUserLocalDataSource(dataSourceImp: UserLocalDataSourceImp): UserLocalDataSource

    @Binds
    @Singleton
    abstract fun bindStatusLocalDataSource(dataSourceImp: StatusLocalDataSourceImp): StatusLocalDataSource

    @Binds
    @Singleton
    abstract fun bindQuoteLocalDataSource(dataSourceImp: QuoteLocalDataSourceImp): QuoteLocalDataSource

    @Binds
    @Singleton
    abstract fun bindMessageLocalDataSource(dataSourceImp: MessageLocalDataSourceImp): MessageLocalDataSource

    @Binds
    @Singleton
    abstract fun bindContactLocalDataSource(dataSourceImp: ContactLocalDataSourceImp): ContactLocalDataSource

    @Binds
    @Singleton
    abstract fun bindAttachmentLocalDataSource(dataSourceImp: AttachmentLocalDataSourceImp): AttachmentLocalDataSource

    @Binds
    @Singleton
    abstract fun bindMessageNotSentLocalDataSource(dataSourceImp: MessageNotSentLocalDataSourceImp): MessageNotSentLocalDataSource

}