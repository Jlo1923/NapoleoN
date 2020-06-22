package com.naposystems.pepito.ui.custom.audioPlayer

import android.net.Uri
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageButton
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.custom.circleProgressBar.CircleProgressBar
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

interface IContractAudioPlayer {
    fun playAudio()
    fun isEncryptedFile(isEncryptedFile: Boolean)
    fun setMessageAndAttachment(messageAndAttachment: MessageAndAttachment)
    fun setMediaPlayerManager(mediaPlayerManager: MediaPlayerManager)
    fun setAudioFileUri(uri: Uri)
    fun setEncryptedFileName(fileName: String)
    fun setAudioId(id: Int)
    fun setListener(listener: AudioPlayerCustomView.Listener)
}