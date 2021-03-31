package com.naposystems.napoleonchat.di.module.mediastore

import com.naposystems.napoleonchat.ui.multi.contract.IContractMultipleAttachment
import com.naposystems.napoleonchat.ui.multi.mediastore.MultipleAttachmentMediaStore
import dagger.Binds
import dagger.Module

@Module
abstract class MediaStoreModule {

    @Binds
    abstract fun bindMultipleAttachmentMediaStore(mediaStore: MultipleAttachmentMediaStore)
            : IContractMultipleAttachment.MediaStore


}