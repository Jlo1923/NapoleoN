package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.repository.previewMedia.PreviewMediaRepository
import com.naposystems.pepito.ui.previewMedia.IContractPreviewMedia
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PreviewMediaModule {

    @Provides
    @Singleton
    fun provideRepository(context: Context): IContractPreviewMedia.Repository {
        return PreviewMediaRepository(context)
    }
}