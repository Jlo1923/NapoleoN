package com.naposystems.napoleonchat.ui.custom.audioPlayer

import android.net.Uri
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager

interface IContractAudioPlayer {
    fun enablePlayButton(isEnable: Boolean)
    fun playAudio()
    fun isEncryptedFile(isEncryptedFile: Boolean)
    fun setMediaPlayerManager(mediaPlayerManager: MediaPlayerManager)
    fun setAudioFileUri(uri: Uri)
    fun setEncryptedFileName(fileName: String)
    fun setMessageId(id: Int)
    fun setListener(listener: AudioPlayerCustomView.Listener)
    fun setDuration(duration: Long)
}