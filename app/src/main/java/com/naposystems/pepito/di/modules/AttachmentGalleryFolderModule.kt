package com.naposystems.pepito.di.modules

import android.content.Context
import com.naposystems.pepito.repository.attachmentGalleryFolder.AttachmentGalleryFolderRepository
import com.naposystems.pepito.ui.attachmentGalleryFolder.IContractAttachmentGalleryFolders
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AttachmentGalleryFolderModule {

    @Provides
    @Singleton
    fun provideRepository(context: Context): IContractAttachmentGalleryFolders.Repository =
        AttachmentGalleryFolderRepository(context)
}