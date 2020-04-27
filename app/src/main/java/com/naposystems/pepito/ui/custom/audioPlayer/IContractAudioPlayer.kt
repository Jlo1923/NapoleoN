package com.naposystems.pepito.ui.custom.audioPlayer

import android.net.Uri
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

interface IContractAudioPlayer {
    fun enablePlayButton(isEnable: Boolean)
    fun setProgress(progress: Long)
    fun playAudio()
    fun isEncryptedFile(isEncryptedFile: Boolean)
    fun setMediaPlayerManager(mediaPlayerManager: MediaPlayerManager)
    fun setAudioFileUri(uri: Uri)
    fun setAudioId(id: Int)
    fun setListener(listener: AudioPlayerCustomView.Listener)
}