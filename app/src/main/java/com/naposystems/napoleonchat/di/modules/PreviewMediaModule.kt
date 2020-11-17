package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.repository.previewMedia.PreviewMediaRepository
import com.naposystems.napoleonchat.ui.previewMedia.IContractPreviewMedia
import com.naposystems.napoleonchat.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PreviewMediaModule {

    @Provides
    @Singleton
    fun provideRepository(
        context: Context,
        napoleonApi: NapoleonApi,
        messageDataSource: MessageDataSource
    ): IContractPreviewMedia.Repository {
        return PreviewMediaRepository(context, napoleonApi, messageDataSource)
    }
}