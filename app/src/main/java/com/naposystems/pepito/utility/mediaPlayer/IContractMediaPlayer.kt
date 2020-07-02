package com.naposystems.pepito.utility.mediaPlayer

import android.net.Uri
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import com.naposystems.pepito.ui.custom.AccessibleToggleButton
import com.naposystems.pepito.ui.custom.animatedTwoVectorView.AnimatedTwoVectorView

interface IContractMediaPlayer {
    fun setAudioId(audioId: String)
    fun setAudioUri(uri: Uri?)
    fun setAudioFileName(fileName: String)
    fun playAudio(progress: Double = 0.0, isEarpiece: Boolean = false)
    fun pauseAudio()
    fun unregisterProximityListener()
    fun isEncryptedFile(isEncryptedFile: Boolean)
    fun setImageButtonPlay(imageButtonPlay: AnimatedTwoVectorView)
    fun setImageButtonSpeed(imageButtonSpeed: ImageButton)
    fun setSeekbar(seekBar: AppCompatSeekBar)
    fun setTextViewDuration(textView: TextView)
    fun setListener(listener: MediaPlayerManager.Listener)
    fun rewindMilliseconds(audioId: String, millis: Long)
    fun forwardMilliseconds(audioId: String, millis: Long)
    fun changeSpeed(audioId: String)
    fun resetMediaPlayer()
    fun setDuration(duration: Long)
}