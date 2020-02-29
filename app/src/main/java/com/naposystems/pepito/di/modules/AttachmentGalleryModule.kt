package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.repository.attachmentGallery.AttachmentGalleryRepository
import com.naposystems.pepito.ui.attachmentGallery.IContractAttachmentGallery
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AttachmentGalleryModule {

    @Provides
    @Singleton
    fun provideRepository(context: Context): IContractAttachmentGallery.Repository =
        AttachmentGalleryRepository(context)
}