package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.repository.attachmentLocation.AttachmentLocationRepository
import com.naposystems.pepito.ui.attachmentLocation.IContractAttachmentLocation
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AttachmentLocationModule {

    @Provides
    @Singleton
    fun provideRepository(context: Context): IContractAttachmentLocation.Repository {
        return AttachmentLocationRepository(context)
    }
}