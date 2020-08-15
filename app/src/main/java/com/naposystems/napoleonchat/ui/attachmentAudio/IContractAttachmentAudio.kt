package com.naposystems.napoleonchat.ui.attachmentAudio

import com.naposystems.napoleonchat.entity.message.attachments.MediaStoreAudio

interface IContractAttachmentAudio {

    interface ViewModel {
        fun loadAudios()
        fun setSelected(mediaStoreAudio: MediaStoreAudio)
        fun getAudiosSelected(): List<MediaStoreAudio>
    }
}