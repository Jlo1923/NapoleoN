package com.naposystems.napoleonchat.utility.sharedViewModels.conversation

import com.naposystems.napoleonchat.model.MediaStoreAudio

interface AudioAttachment {

    fun setAudiosSelected(listMediaStoreAudio: List<MediaStoreAudio>)
    fun getAudiosSelected(): List<MediaStoreAudio>
    fun setAudioSendClicked()
    fun resetAudioSendClicked()

}