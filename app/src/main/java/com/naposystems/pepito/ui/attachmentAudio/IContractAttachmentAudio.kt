package com.naposystems.pepito.ui.attachmentAudio

import com.naposystems.pepito.entity.message.attachments.MediaStoreAudio

interface IContractAttachmentAudio {

    interface ViewModel {
        fun loadAudios()
        fun setSelected(mediaStoreAudio: MediaStoreAudio)
        fun getAudiosSelected(): List<MediaStoreAudio>
    }
}