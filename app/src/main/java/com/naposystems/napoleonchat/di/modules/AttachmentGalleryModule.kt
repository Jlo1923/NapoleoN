package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.repository.attachmentGallery.AttachmentGalleryRepository
import com.naposystems.napoleonchat.ui.attachmentGallery.IContractAttachmentGallery
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