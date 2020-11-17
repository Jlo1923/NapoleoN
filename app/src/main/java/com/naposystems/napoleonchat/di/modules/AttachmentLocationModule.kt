package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.repository.attachmentLocation.AttachmentLocationRepository
import com.naposystems.napoleonchat.ui.attachmentLocation.IContractAttachmentLocation
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