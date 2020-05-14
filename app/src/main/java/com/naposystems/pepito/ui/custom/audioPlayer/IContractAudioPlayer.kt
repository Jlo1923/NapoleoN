package com.naposystems.pepito.ui.custom.audioPlayer

import android.net.Uri
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageButton
import com.naposystems.pepito.ui.custom.circleProgressBar.CircleProgressBar
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager

interface IContractAudioPlayer {
    fun enablePlayButton(isEnable: Boolean)
    fun getProgressBar(): CircleProgressBar
    fun setProgress(progress: Long)
    fun hideProgressBar()
    fun getIndeterminateProgress(): ProgressBar
    fun showIndeterminateProgress()
    fun hideIndeterminateProgress()
    fun changeImageButtonStateIcon(icon: Int)
    fun getImageButtonState(): AppCompatImageButton
    fun showImageButtonState()
    fun hideImageButtonState()
    fun playAudio()
    fun isEncryptedFile(isEncryptedFile: Boolean)
    fun setMediaPlayerManager(mediaPlayerManager: MediaPlayerManager)
    fun setAudioFileUri(uri: Uri)
    fun setAudioId(id: Int)
    fun setListener(listener: AudioPlayerCustomView.Listener)
}