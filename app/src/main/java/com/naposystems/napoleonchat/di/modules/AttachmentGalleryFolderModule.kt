package com.naposystems.napoleonchat.di.modules

import android.content.Context
import com.naposystems.napoleonchat.repository.attachmentGalleryFolder.AttachmentGalleryFolderRepository
import com.naposystems.napoleonchat.ui.attachmentGalleryFolder.IContractAttachmentGalleryFolders
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