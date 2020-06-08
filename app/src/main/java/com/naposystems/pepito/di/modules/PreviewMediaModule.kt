package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.repository.previewMedia.PreviewMediaRepository
import com.naposystems.pepito.ui.previewMedia.IContractPreviewMedia
import com.naposystems.pepito.webService.NapoleonApi
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