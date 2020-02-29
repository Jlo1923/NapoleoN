package com.naposystems.pepito.ui.custom.audioPlayer

import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

interface IContractAudioPlayer {
    fun playAudio()
    fun isEncryptedFile(isEncryptedFile: Boolean)
    fun setMediaPlayerManager(mediaPlayerManager: MediaPlayerManager)
    fun setAbsolutePath(absolutePath: String)
    fun setAudioId(id: Int)
    fun setListener(listener: AudioPlayerCustomView.Listener)
}